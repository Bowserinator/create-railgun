package net.hellomouse.createrailgun.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;

import net.hellomouse.createrailgun.registry.CRBlockEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.ProjectileBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.he_shell.HEShellBlock;
import rbasamoyai.createbigcannons.munitions.big_cannon.he_shell.HEShellProjectile;

public class BurstShellBlock extends HEShellBlock {
    private static final MapCodec<ProjectileBlock> CODEC = simpleCodec(BurstShellBlock::new);
    private final Supplier<EntityType<? extends HEShellProjectile>> entityTypeSupplier;

    public BurstShellBlock(BlockBehaviour.Properties p) {
        this(p, null);
    }

    public BurstShellBlock(BlockBehaviour.Properties p, Supplier<EntityType<? extends HEShellProjectile>> entityType) {
        super(p);
        this.entityTypeSupplier = entityType;
    }

    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    public BlockEntityType<? extends FuzedBlockEntity> getBlockEntityType() {
        return CRBlockEntities.FUZED_BLOCK.get();
    }

    public EntityType<? extends HEShellProjectile> getAssociatedEntityType() {
        return this.entityTypeSupplier != null ? this.entityTypeSupplier.get() : super.getAssociatedEntityType();
    }
}
