package net.hellomouse.createrailgun.registry;

import net.hellomouse.createrailgun.CreateRailgun;
import net.hellomouse.createrailgun.block.BurstShellBlock;
import net.hellomouse.createrailgun.block.RailgunCoreBlock;
import net.hellomouse.createrailgun.block.RailgunRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CRBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateRailgun.MODID);
    public static final DeferredBlock<Block> BURST_SHELL_BLOCK = BLOCKS.register("burst_shell", () -> new BurstShellBlock(shellProps().mapColor(MapColor.COLOR_RED),
            CREntities.BURST_SHELL::get));

    private static final BlockBehaviour.Properties RAILGUN_PROPS = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops()
            .strength(5.0F, 9.0F).sound(SoundType.METAL)
            .isRedstoneConductor((state, level, pos) -> false);
    public static final DeferredBlock<RailgunCoreBlock> RAILGUN_CORE = BLOCKS.register("railgun_core", () -> new RailgunCoreBlock(RAILGUN_PROPS));
    public static final DeferredBlock<RailgunRailBlock> RAILGUN_RAIL = BLOCKS.register("railgun_rail", () -> new RailgunRailBlock(RAILGUN_PROPS));

    public static BlockBehaviour.Properties shellProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(2.0F, 3.0F)
                .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops();
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
