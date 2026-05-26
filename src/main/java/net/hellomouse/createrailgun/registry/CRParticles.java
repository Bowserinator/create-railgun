package net.hellomouse.createrailgun.registry;

import net.hellomouse.createrailgun.CreateRailgun;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CRParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, CreateRailgun.MODID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RAILGUN_SHOCKWAVE = PARTICLE_TYPES.register("railgun_shockwave", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GLOWING_DUST = PARTICLE_TYPES.register("glowing_dust", () -> new SimpleParticleType(true));


    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
