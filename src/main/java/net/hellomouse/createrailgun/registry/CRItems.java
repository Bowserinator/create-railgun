package net.hellomouse.createrailgun.registry;

import net.hellomouse.createrailgun.CreateRailgun;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CRItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateRailgun.MODID);
    public static final DeferredItem<BlockItem> BURST_SHELL = ITEMS.registerSimpleBlockItem(CRBlocks.BURST_SHELL_BLOCK);
    public static final DeferredItem<BlockItem> RAILGUN_CORE = ITEMS.registerSimpleBlockItem(CRBlocks.RAILGUN_CORE);
    public static final DeferredItem<BlockItem> RAILGUN_RAIL = ITEMS.registerSimpleBlockItem(CRBlocks.RAILGUN_RAIL);
    public static final DeferredItem<BlockItem> CORDIUM_CRYSTAL_BLOCK = ITEMS.register("cordium_crystal_block",
            () -> new BlockItem(CRBlocks.CORDIUM_CRYSTAL_BLOCK.get(), new Item.Properties()) {
                @Override
                public int getBurnTime(@NotNull ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                    return 32000;
                }
            }
    );

    public static final DeferredItem<Item> NETHERITE_FILTER = ITEMS.register("netherite_filter",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<Item> MAGMA_SLAG = ITEMS.register("magma_slag",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FILTERED_MAGMA_SLAG = ITEMS.register("filtered_magma_slag",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CORDIUM_CRYSTAL = ITEMS.register("cordium_crystal",
            () -> new Item(new Item.Properties()) {
                @Override
                public int getBurnTime(@NotNull ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                    return 3200;
                }
            });
    public static final DeferredItem<Item> RAILGUN_SLUG = ITEMS.register("railgun_slug",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
