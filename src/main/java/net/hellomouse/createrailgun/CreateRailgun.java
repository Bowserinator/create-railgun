package net.hellomouse.createrailgun;

import com.mojang.logging.LogUtils;
import net.hellomouse.createrailgun.registry.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateRailgun.MODID)
public class CreateRailgun {
    public static final String MODID = "createrailgun";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOD_TAB = CREATIVE_MODE_TABS.register("createrailgun_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.createrailgun"))
            .icon(() -> CRItems.RAILGUN_RAIL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CRItems.MAGMA_SLAG.get());
                output.accept(CRItems.NETHERITE_FILTER.get());
                output.accept(CRItems.FILTERED_MAGMA_SLAG.get());
                output.accept(CRItems.CORDIUM_CRYSTAL.get());
                output.accept(CRItems.CORDIUM_CRYSTAL_BLOCK.get());
                output.accept(CRItems.RAILGUN_SLUG.get());
                output.accept(CRItems.BURST_SHELL.get());
                output.accept(CRItems.RAILGUN_RAIL.get());
                output.accept(CRItems.RAILGUN_CORE.get());
            }).build());

    public CreateRailgun(IEventBus modEventBus, ModContainer modContainer) {
        CREATIVE_MODE_TABS.register(modEventBus);
        CRBlocks.register(modEventBus);
        CRParticles.register(modEventBus);
        CREntities.register(modEventBus);
        CRBlockEntities.register(modEventBus);
        CRItems.register(modEventBus);
        CRDataComponents.DATA_COMPONENTS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CREntities::registerHandlers);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) { // Require at least 1 @SubscribeEvent
        LOGGER.info("{} starting on server...", MODID);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CRBlockEntities.RAILGUN_CORE.get(), (be, side) -> be.inventory);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, CRBlockEntities.RAILGUN_CORE.get(), (be, side) -> be.energy);
    }
}
