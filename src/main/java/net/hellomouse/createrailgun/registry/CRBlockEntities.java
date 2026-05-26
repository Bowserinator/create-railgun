package net.hellomouse.createrailgun.registry;

import foundry.veil.platform.registry.RegistryObject;
import net.hellomouse.createrailgun.CreateRailgun;
import net.hellomouse.createrailgun.block.be.RailgunCoreBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileBlockEntity;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

public class CRBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CreateRailgun.MODID);
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<FuzedBlockEntity>> fuzedRef;
    private static DeferredHolder<BlockEntityType<?>, BlockEntityType<BigCannonProjectileBlockEntity>> projectileRef;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FuzedBlockEntity>> FUZED_BLOCK;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BigCannonProjectileBlockEntity>> PROJECTILE_BLOCK;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RailgunCoreBlockEntity>> RAILGUN_CORE =
            BLOCK_ENTITY_TYPES.register("railgun_core", () -> BlockEntityType.Builder.of(RailgunCoreBlockEntity::new, CRBlocks.RAILGUN_CORE.get()).build(null));

    static {
        FUZED_BLOCK = BLOCK_ENTITY_TYPES.register("fuzed_block", () -> Builder.of((pos, state) -> new FuzedBlockEntity(fuzedRef.get(), pos, state),
                new Block[]{CRBlocks.BURST_SHELL_BLOCK.get()}).build(null));
        fuzedRef = FUZED_BLOCK;
        PROJECTILE_BLOCK = BLOCK_ENTITY_TYPES.register("projectile_block", () -> Builder.of((pos, state) -> new BigCannonProjectileBlockEntity(projectileRef.get(), pos, state), new Block[]{}).build(null));
        projectileRef = PROJECTILE_BLOCK;
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
