package net.hellomouse.createrailgun.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class GlowingDustParticle extends DustParticle {
    public static final int MAX_LIFE = 30;
    private final float baseSize;

    public GlowingDustParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, DustParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz, options, sprites);
        this.lifetime = MAX_LIFE;
        this.baseSize = options.getScale();

        this.rCol = 0.1f;
        this.gCol = 0.5f;
        this.bCol = 1.0f;
        this.alpha = 1.0f;
    }

    @Override
    public void tick() {
        super.tick();
        float lifeRatio = (float) this.age / (float) this.lifetime;
        this.alpha = 1.0f - lifeRatio;

        // Fade color from Blue (0.1, 0.5, 1.0) to Orange (1.0, 0.4, 0.0)
        this.rCol = Mth.lerp(lifeRatio, 0.1f, 1.0f);
        this.gCol = Mth.lerp(lifeRatio, 0.5f, 0.4f);
        this.bCol = Mth.lerp(lifeRatio, 1.0f, 0.0f);
    }

    @Override
    public float getQuadSize(float partialTick) {
        float currentAge = (float) this.age + partialTick;
        float lifeRatio = Mth.clamp(currentAge / (float) this.lifetime, 0.0f, 1.0f);
        return this.baseSize * (1.0f - lifeRatio);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880; // Full brightness
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(@NotNull SimpleParticleType arg, @NotNull ClientLevel arg2, double x, double y, double z, double lookX, double lookY, double lookZ) {
            return new GlowingDustParticle(arg2, x, y, z, lookX, lookY, lookZ, new DustParticleOptions(new Vector3f(1.0f, 0.4f, 0.0f), 1.0f), this.spriteProvider);
        }
    }
}