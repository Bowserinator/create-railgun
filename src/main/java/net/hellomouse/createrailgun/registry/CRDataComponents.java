package net.hellomouse.createrailgun.registry;

import net.hellomouse.createrailgun.CreateRailgun;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CRDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =  DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, CreateRailgun.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> LOADED_SHELLS;

    static {
        LOADED_SHELLS = DATA_COMPONENTS.register("loaded_shells", () -> {
            return DataComponentType.<ItemContainerContents>builder().persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).build();
        });
    }
}
