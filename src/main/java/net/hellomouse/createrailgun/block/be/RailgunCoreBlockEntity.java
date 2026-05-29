package net.hellomouse.createrailgun.block.be;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.createmod.catnip.lang.Lang;
import net.hellomouse.createrailgun.Config;
import net.hellomouse.createrailgun.CreateRailgun;
import net.hellomouse.createrailgun.block.RailgunCoreBlock;
import net.hellomouse.createrailgun.block.RailgunRailBlock;
import net.hellomouse.createrailgun.compat.SableCompat;
import net.hellomouse.createrailgun.entity.RailgunSlugEntity;
import net.hellomouse.createrailgun.registry.*;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import rbasamoyai.createbigcannons.index.CBCSoundEvents;

import java.util.List;
import java.util.Objects;

public class RailgunCoreBlockEntity extends BlockEntity implements IHaveGoggleInformation {
    public static final int INVENTORY_SIZE = 1;
    public static final int MAX_RAILS = 7;

    private int lastLightStage = 0;
    private int currentCooldown = 0;
    private int cachedRailCount = 0;
    private int chargeTicksLeft = 0;
    private long cooldownEndTick = 0;
    private long prevCharge = 0;
    private boolean sendBlockUpdateThisTick = false;

    public final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(CRItems.RAILGUN_SLUG.get());
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sendBlockUpdate();
        }
    };

    public final EnergyStorage energy = new EnergyStorage(GET_BASE_ENERGY_CAPACITY());

    public RailgunCoreBlockEntity(BlockPos pos, BlockState blockState) {
        super(CRBlockEntities.RAILGUN_CORE.get(), pos, blockState);
    }

    private void sendBlockUpdate() {
        if (level instanceof ServerLevel serverLevel)
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        // Update on energy change
        long currentCharge = energy.getEnergyStored();
        if (prevCharge != currentCharge) {
            prevCharge = currentCharge;
            this.sendBlockUpdateThisTick = true;
        }

        if (currentCooldown > 0) {
            currentCooldown--;
            int currentStage = (int) Math.ceil(((double) currentCooldown / GET_COOLDOWN_TICKS()) * 3);
            if (currentStage != lastLightStage) {
                lastLightStage = currentStage;
                setRailsBrightness(currentStage);
            } else if (level instanceof ServerLevel serverLevel && level.getGameTime() % 3 == 0)
                spawnParticlesAlongRail(serverLevel, ParticleTypes.SMOKE, 1, 0.2, 0, 0.2, 0.01, 0.5, 1, 0.5);
        } else if (chargeTicksLeft > 0) {
            chargeTicksLeft--;
            if (level instanceof ServerLevel serverLevel && level.getGameTime() % 4 == 0)
                spawnParticlesAlongRail(serverLevel, CRParticles.GLOWING_DUST.get(), 2, 0.5, 0.5, 0.5, 0.01, 0.5, 0.5, 0.5);
            if (chargeTicksLeft <= 0)
                fire(findAmmoSlot());
        }

        if (sendBlockUpdateThisTick) {
            sendBlockUpdate();
            sendBlockUpdateThisTick = false;
        }
    }

    public void trigger() {
        this.sendBlockUpdateThisTick = true;
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
        chargeTicksLeft = GET_MAX_CHARGE_TICKS_LEFT();
        energy.extractEnergy(energy.getEnergyStored(), false); // Consume the entire buffer
        setRailsBrightness(3); // 3 = max brightness
    }

    public double getMuzzleVelocity() {
        return 1.5 + cachedRailCount / (double) MAX_RAILS * 3;
    }

    private void rewardFireAdvancement() {
        if (this.level instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath("createrailgun", "fire_railgun");
            AdvancementHolder advancement = server.getAdvancements().get(advancementId);
            if (advancement == null)
                return;

            AABB searchArea = new AABB(worldPosition).inflate(8.0);
            List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, searchArea);

            for (ServerPlayer player : nearbyPlayers) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone())
                    player.getAdvancements().award(advancement, "fire_railgun");
            }
        }
    }

    private void fire(int ammoSlot) {
        inventory.extractItem(ammoSlot, 1, false);
        currentCooldown = GET_COOLDOWN_TICKS();
        cooldownEndTick = level.getGameTime() + currentCooldown;
        this.lastLightStage = 3;
        setRailsBrightness(3);
        this.rewardFireAdvancement();

        var level = (ServerLevel) this.level;

        Vector3d sableSubContrapationVelocity = new Vector3d(0);
        if (ModList.get().isLoaded("sable"))
            sableSubContrapationVelocity = SableCompat.getSableSubContrapationVelocity(level, worldPosition);

        assert level != null;
        RailgunSlugEntity slug = CREntities.RAILGUN_PROJECTILE.get().create(level);
        if (slug != null) {
            Direction facing = this.getBlockState().getValue(RailgunCoreBlock.FACING);
            double offsetDistance = cachedRailCount;
            double speed = getMuzzleVelocity();
            Vec3 vel = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
            Vec3 pos = Vec3.atCenterOf(this.getBlockPos()).add(vel.scale(offsetDistance));

            float dot = 0.0F;
            if (ModList.get().isLoaded("sable"))
                dot = SableCompat.getDotWithFacing(level, worldPosition, facing, sableSubContrapationVelocity);

            vel = vel.scale(speed + dot);
            pos = pos.add(vel);

            slug.moveTo(pos.x, pos.y, pos.z);
            slug.setDeltaMovement(vel);
            level.addFreshEntity(slug);
            level.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 0, 0, 0);
            level.playSound(null, getBlockPos(), CBCSoundEvents.FIRE_BIG_CANNON.getMainEvent(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
        this.sendBlockUpdateThisTick = true;
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
            this.sendBlockUpdateThisTick = true;
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
        level.sendParticles(particle, worldPosition.getX() + dx, worldPosition.getY() + dy, worldPosition.getZ() + dz, count, xOffset, yOffset, zOffset, speed);
        for (int i = 1; i <= cachedRailCount; i++) {
            BlockPos railPos = worldPosition.relative(facing, i);
            level.sendParticles(particle, railPos.getX() + 0.5, railPos.getY() + 0.5, railPos.getZ() + 0.5, count, xOffset, yOffset, zOffset, speed);
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
        tag.putLong("CooldownEndTick", cooldownEndTick);
        tag.putInt("RailCount", cachedRailCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        energy.deserializeNBT(registries, Objects.requireNonNull(tag.get("Energy")));
        currentCooldown = tag.getInt("Cooldown");
        chargeTicksLeft = tag.getInt("FireCooldown");
        cooldownEndTick = tag.getLong("CooldownEndTick");
        cachedRailCount = tag.getInt("RailCount");
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (cachedRailCount == 0 || cachedRailCount > MAX_RAILS) {
            Lang.builder(CreateRailgun.MODID).add(Component.literal("Invalid Railgun ").withStyle(ChatFormatting.RED)).forGoggles(tooltip);
            if (cachedRailCount == 0)
                Lang.builder(CreateRailgun.MODID).add(Component.literal("No railgun rails found").withStyle(ChatFormatting.GRAY)).forGoggles(tooltip);
            else if (cachedRailCount > MAX_RAILS)
                Lang.builder(CreateRailgun.MODID).add(Component.literal("Too many rails!").withStyle(ChatFormatting.GRAY)).forGoggles(tooltip);
            return true;
        }

        int ammoSlot = findAmmoSlot();
        int ammo = ammoSlot >= 0 ? inventory.getStackInSlot(ammoSlot).getCount() : 0;

        Lang.builder(CreateRailgun.MODID)
                .add(Component.literal("⚡ ").withStyle(ChatFormatting.GRAY))
                .add(Component.literal(energy.getEnergyStored() + " / " + GET_BASE_ENERGY_CAPACITY() + "  ").withStyle(ChatFormatting.AQUA))
                .add(Component.translatable("tooltip.createrailgun.ammo").withStyle(ChatFormatting.GRAY))
                .add(Component.literal(" " + ammo).withStyle(ChatFormatting.YELLOW))
                .forGoggles(tooltip);
        Lang.builder(CreateRailgun.MODID)
                .add(Component.translatable("tooltip.createrailgun.muzzle_velocity").withStyle(ChatFormatting.GRAY))
                .add(Component.literal(String.format(" %.1f m/s", 20 * getMuzzleVelocity())).withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip);

        assert level != null;
        long cooldown = cooldownEndTick - level.getGameTime();
        if (cooldown > 0)
            Lang.builder(CreateRailgun.MODID)
                    .add(Component.literal("⏱ ").withStyle(ChatFormatting.GRAY))
                    .add(Component.literal(String.format("%.1fs", cooldown / 20.0)).withStyle(ChatFormatting.RED))
                    .forGoggles(tooltip);

        return true;
    }

    public static int GET_BASE_ENERGY_CAPACITY() {
        return Config.POWER_BUFFER_REQUIRED.getAsInt();
    }

    public static int GET_COOLDOWN_TICKS() {
        return Config.COOLDOWN_TICKS.getAsInt();
    }

    public static int GET_MAX_CHARGE_TICKS_LEFT() {
        return Config.CHARGE_TIME_TICKS.getAsInt();
    }
}
