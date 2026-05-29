package net.hellomouse.createrailgun.block;

import net.hellomouse.createrailgun.registry.CRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.index.CBCDamageTypes;
import rbasamoyai.createbigcannons.munitions.CannonDamageSource;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;

import javax.annotation.Nullable;

public class CordiumCrystalBlock extends TntBlock {
    public CordiumCrystalBlock(BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(1.5f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.AMETHYST)
                .lightLevel(state -> 4)
                .randomTicks()
                .noOcclusion()
        );
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        explode(level, pos);
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, Direction face, @Nullable LivingEntity igniter) {
        explode(level, pos);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        ItemStack held = player.getMainHandItem();

        if (held.is(Items.FLINT_AND_STEEL) || held.is(Items.FIRE_CHARGE)) {
            explode(level, pos);
            if (!player.getAbilities().instabuild)
                held.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    public static void explode(Level level, @NotNull BlockPos pos) {
        if (level.isClientSide) return;

        level.removeBlock(pos, false);
        ShellExplosion explosion = new ShellExplosion(level, null,
                new CannonDamageSource(CannonDamageSource.getDamageRegistry(level).getHolderOrThrow(CBCDamageTypes.SHRAPNEL), false),
                pos.getX(), pos.getY(), pos.getZ(), 7,
                false, Explosion.BlockInteraction.DESTROY, false);
        CreateBigCannons.handleCustomExplosion(level, explosion);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (random.nextFloat() < 0.35f) {
            double x = pos.getX() + random.nextDouble() * 5;
            double y = pos.getY() + random.nextDouble() * 5;
            double z = pos.getZ() + random.nextDouble() * 5;
            level.addParticle(DustParticleOptions.REDSTONE, x, y, z, 1.0, 0.4, 0.0);
        }
    }
}
