package net.hellomouse.createrailgun;

import net.createmod.ponder.foundation.PonderIndex;
import net.hellomouse.createrailgun.client.entity.RailgunSlugModel;
import net.hellomouse.createrailgun.client.particle.GlowingDustParticle;
import net.hellomouse.createrailgun.client.particle.RailgunShockwaveParticle;
import net.hellomouse.createrailgun.client.render.RailgunSlugRenderer;
import net.hellomouse.createrailgun.ponder.CRPonderPlugin;
import net.hellomouse.createrailgun.registry.CREntities;
import net.hellomouse.createrailgun.registry.CRParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import rbasamoyai.createbigcannons.munitions.big_cannon.BigCannonProjectileRenderer;

@Mod(value = CreateRailgun.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CreateRailgun.MODID, value = Dist.CLIENT)
public class CreateRailgunClient {
    public CreateRailgunClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> { PonderIndex.addPlugin(new CRPonderPlugin()); });
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RailgunSlugModel.LAYER_LOCATION, RailgunSlugModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CREntities.RAILGUN_PROJECTILE.get(), RailgunSlugRenderer::new);
        event.registerEntityRenderer(CREntities.BURST_SHELL.get(), BigCannonProjectileRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(final RegisterParticleProvidersEvent event) {
        CreateRailgun.LOGGER.debug("Registered particle factories");
        event.registerSpriteSet(CRParticles.RAILGUN_SHOCKWAVE.get(), RailgunShockwaveParticle.Factory::new);
        event.registerSpriteSet(CRParticles.GLOWING_DUST.get(), GlowingDustParticle.Factory::new);
    }
}
