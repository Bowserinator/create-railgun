package net.hellomouse.createrailgun.registry;

import net.hellomouse.createrailgun.CreateRailgun;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CRItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateRailgun.MODID);
    public static final DeferredItem<BlockItem> BURST_SHELL = ITEMS.registerSimpleBlockItem(CRBlocks.BURST_SHELL_BLOCK);
    public static final DeferredItem<BlockItem> RAILGUN_CORE = ITEMS.registerSimpleBlockItem(CRBlocks.RAILGUN_CORE);
    public static final DeferredItem<BlockItem> RAILGUN_RAIL = ITEMS.registerSimpleBlockItem(CRBlocks.RAILGUN_RAIL);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
