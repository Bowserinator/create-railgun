package net.hellomouse.createrailgun.block.be;

import net.hellomouse.createrailgun.Config;
import net.hellomouse.createrailgun.block.RailgunCoreBlock;
import net.hellomouse.createrailgun.block.RailgunRailBlock;
import net.hellomouse.createrailgun.entity.RailgunSlugEntity;
import net.hellomouse.createrailgun.registry.CRBlockEntities;
import net.hellomouse.createrailgun.registry.CREntities;
import net.hellomouse.createrailgun.registry.CRParticles;
import net.hellomouse.createrailgun.registry.CRSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;

import java.util.Objects;

public class RailgunCoreBlockEntity extends BlockEntity {
    public static final int INVENTORY_SIZE = 1;
    public static final int BASE_ENERGY_CAPACITY = Config.POWER_BUFFER_REQUIRED.getAsInt();
    public static final int MAX_RAILS = 5;
    public static final int COOLDOWN_TICKS = Config.COOLDOWN_TICKS.getAsInt();
    public static final int MAX_CHARGE_TICKS_LEFT = Config.CHARGE_TIME_TICKS.getAsInt();

    private int lastLightStage = 0;
    private int currentCooldown = 0;
    private int cachedRailCount = 0;
    private int chargeTicksLeft = 0;

    public final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(Items.IRON_INGOT);
        }
        @Override
        protected void onContentsChanged(int slot) {setChanged(); }
    };

    public final EnergyStorage energy = new EnergyStorage(BASE_ENERGY_CAPACITY);

    public RailgunCoreBlockEntity(BlockPos pos, BlockState blockState) {
        super(CRBlockEntities.RAILGUN_CORE.get(), pos, blockState);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        if (currentCooldown > 0) {
            currentCooldown--;

            int currentStage = (int) Math.ceil(((double) currentCooldown / COOLDOWN_TICKS) * 3);
            if (currentStage != lastLightStage) {
                lastLightStage = currentStage;
                setRailsBrightness(currentStage);
            }
            else if (level instanceof ServerLevel serverLevel && level.getGameTime() % 3 == 0)
                spawnParticlesAlongRail(serverLevel, ParticleTypes.SMOKE, 1, 0.2, 0, 0.2, 0.01, 0.5, 1, 0.5);
        } else if (chargeTicksLeft > 0) {
            chargeTicksLeft--;
            if (level instanceof ServerLevel serverLevel && level.getGameTime() % 4 == 0)
                spawnParticlesAlongRail(serverLevel, CRParticles.GLOWING_DUST.get(), 2, 0.5, 0.5, 0.5, 0.01, 0.5, 0.5, 0.5);
            if (chargeTicksLeft <= 0)
                fire(findAmmoSlot());
        }
    }

    public void trigger() {
        if (chargeTicksLeft > 0 || !(level instanceof ServerLevel)) return;

        updateMultiblock();
        var pos = this.getBlockPos().getCenter();
        if (cachedRailCount == 0 || cachedRailCount > MAX_RAILS) return;

        if (currentCooldown > 0) {
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
            return;
        }
        if (energy.getEnergyStored() < energy.getMaxEnergyStored()) {
            level.playSound(null, pos.x, pos.y, pos.z, CRSounds.LOW_POWER, SoundSource.BLOCKS, 0.5f, 0.5f);
            return;
        }
        if (findAmmoSlot() == -1) {
            level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0f, 1.0f);
            return;
        }
        level.playSound(null, pos.x, pos.y, pos.z, CRSounds.RAILGUN_CHARGE, SoundSource.BLOCKS, 1.0f, 1.0f);
        chargeTicksLeft = MAX_CHARGE_TICKS_LEFT;
        setRailsBrightness(3); // 3 = max brightness
    }

    private void fire(int ammoSlot) {
        inventory.extractItem(ammoSlot, 1, false);
        energy.extractEnergy(energy.getEnergyStored(), false); // Consume the entire buffer
        currentCooldown = COOLDOWN_TICKS;
        this.lastLightStage = 3;
        setRailsBrightness(3);

        var level = (ServerLevel)this.level;

        RailgunSlugEntity slug = CREntities.RAILGUN_PROJECTILE.get().create(level);
        if (slug != null) {
            Direction facing = this.getBlockState().getValue(RailgunCoreBlock.FACING);
            double offsetDistance = cachedRailCount + 0.5;
            double speed = 1.5 + cachedRailCount / (double)MAX_RAILS * 3;
            Vec3 vel = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
            Vec3 pos = Vec3.atCenterOf(this.getBlockPos()).add(vel.scale(offsetDistance));

            slug.moveTo(pos.x, pos.y, pos.z);
            slug.setDeltaMovement(vel.scale(speed));
            level.addFreshEntity(slug);
            level.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 0, 0, 0);
            level.playSound(null, getBlockPos(), CBCSoundEvents.FIRE_BIG_CANNON.getMainEvent(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
    }

    public void updateMultiblock() {
        Direction facing = getBlockState().getValue(RailgunCoreBlock.FACING);
        int railsFound = 0;
        for (int i = 1; i <= MAX_RAILS + 2; i++) { // +1 to disallow too many rails
            BlockPos checkPos = worldPosition.relative(facing, i);
            assert level != null;
            BlockState state = level.getBlockState(checkPos);
            if (!(state.getBlock() instanceof RailgunRailBlock && (state.getValue(RailgunRailBlock.FACING) == facing || state.getValue(RailgunRailBlock.FACING).getOpposite() == facing)))
                break;
            railsFound++;
        }

        if (railsFound != cachedRailCount) {
            cachedRailCount = railsFound;
            if (level instanceof ServerLevel serverLevel) {
                if (cachedRailCount > 0 && cachedRailCount <= MAX_RAILS)
                    spawnParticlesAlongRail(serverLevel, ParticleTypes.HAPPY_VILLAGER, 8, 0.5, 0.5, 0.5, 0, 0.5, 0.5, 0.5);
                else
                    spawnParticlesAlongRail(serverLevel, DustParticleOptions.REDSTONE, 8, 0.5, 0.5, 0.5, 0, 0.5, 0.5, 0.5);
            }
        }
    }

    private void setRailsBrightness(int stage) {
        if (level == null || level.isClientSide) return;
        Direction facing = getBlockState().getValue(RailgunCoreBlock.FACING);
        for (int i = 1; i <= cachedRailCount; i++) {
            BlockPos railPos = worldPosition.relative(facing, i);
            BlockState state = level.getBlockState(railPos);
            if (state.getBlock() instanceof RailgunRailBlock && state.getValue(RailgunRailBlock.BRIGHTNESS) != stage)
                level.setBlock(railPos, state.setValue(RailgunRailBlock.BRIGHTNESS, stage), 3);
        }
    }

    private void spawnParticlesAlongRail(ServerLevel level, ParticleOptions particle, int count, double xOffset, double yOffset, double zOffset, double speed,
                                         double dx, double dy, double dz) {
        Direction facing = getBlockState().getValue(RailgunCoreBlock.FACING);
        level.sendParticles(particle,worldPosition.getX() + dx, worldPosition.getY() + dy, worldPosition.getZ() + dz, count, xOffset, yOffset, zOffset, speed);
        for (int i = 1; i <= cachedRailCount; i++) {
            BlockPos railPos = worldPosition.relative(facing, i);
            level.sendParticles(particle,railPos.getX() + 0.5, railPos.getY() + 0.5, railPos.getZ() + 0.5, count, xOffset, yOffset, zOffset, speed);
        }
    }

    private int findAmmoSlot() {
        for (int i = 0; i < inventory.getSlots(); i++)
            if (!inventory.getStackInSlot(i).isEmpty()) return i;
        return -1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Cooldown", currentCooldown);
        tag.putInt("FireCooldown", chargeTicksLeft);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        energy.deserializeNBT(registries, Objects.requireNonNull(tag.get("Energy")));
        currentCooldown = tag.getInt("Cooldown");
        chargeTicksLeft = tag.getInt("FireCooldown");
    }
}
