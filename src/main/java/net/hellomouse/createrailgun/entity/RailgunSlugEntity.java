package net.hellomouse.createrailgun.entity;

import net.hellomouse.createrailgun.Config;
import net.hellomouse.createrailgun.registry.CRParticles;
import net.hellomouse.createrailgun.client.Trail;
import net.hellomouse.createrailgun.client.TrailManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.index.CBCDamageTypes;
import rbasamoyai.createbigcannons.munitions.CannonDamageSource;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;

public class RailgunSlugEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> DATA_LIFE = SynchedEntityData.defineId(RailgunSlugEntity.class, EntityDataSerializers.INT);

    private static final double DAMAGE = 40.0;
    private static final int DEFAULT_LIFE = 100;
    private float explosionPower = Config.EXPLOSION_POWER.get();
    private int life = DEFAULT_LIFE;

    public RailgunSlugEntity(EntityType<RailgunSlugEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public RailgunSlugEntity(EntityType<? extends ThrowableProjectile> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
        this.setNoGravity(true);
    }

    public RailgunSlugEntity(EntityType<? extends ThrowableProjectile> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level);
        this.setNoGravity(true);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(DATA_LIFE, DEFAULT_LIFE);
    }

    @Override
    public void tick() {
        super.tick();

        boolean isClient = this.level().isClientSide;
        if (isClient && (this.tickCount == 1 || this.life % 4 == 0)) // Add in case client only sees it later
            TrailManager.addTrail(new Trail(this.getId(), this.position().subtract(this.getDeltaMovement())));

        if (!isClient) {
            if (this.life > 0) {
                this.life--;
                this.entityData.set(DATA_LIFE, this.life);
                if (this.life <= 0)
                    this.explode();
                else if (this.life % 5 == 0)
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.LIGHTNING_BOLT_THUNDER,
                            net.minecraft.sounds.SoundSource.NEUTRAL, 2.0f, 2.1f);
            }
        } else {
            this.life = this.entityData.get(DATA_LIFE);
            if (this.life % 2 == 0) {
                var velocity = this.getDeltaMovement().normalize();
                this.level().addParticle(CRParticles.RAILGUN_SHOCKWAVE.get(), true,
                        this.getX(), this.getY(), this.getZ(), velocity.x, velocity.y, velocity.z);
            }

            double offsetX = (random.nextDouble() - 0.5) * 2.0;
            double offsetY = (random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (random.nextDouble() - 0.5) * 2.0;

            this.level().addParticle(CRParticles.GLOWING_DUST.get(), true,
                    this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 0, 0, 0);
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), (float) DAMAGE);
            this.explode();
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide)
            this.explode();
    }

    private void explode() {
        ShellExplosion explosion = new ShellExplosion(this.level(), this,
                new CannonDamageSource(CannonDamageSource.getDamageRegistry(this.level()).getHolderOrThrow(CBCDamageTypes.CANNON_PROJECTILE), false),
                this.getX(), this.getY(), this.getZ(), this.explosionPower,
                false, Explosion.BlockInteraction.DESTROY, false);
        CreateBigCannons.handleCustomExplosion(this.level(), explosion);
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("ExplosionPower", this.explosionPower);
        tag.putInt("Life", this.life);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("ExplosionPower"))
            this.explosionPower = tag.getFloat("ExplosionPower");
        if (tag.contains("Life"))
            this.life = tag.getInt("Life");
    }

    public RailgunSlugEntity setExplosionPower(float power) {
        this.explosionPower = power;
        return this;
    }

    public RailgunSlugEntity setLife(int lifeTicks) {
        this.life = lifeTicks;
        this.entityData.set(DATA_LIFE, lifeTicks);
        return this;
    }

    public int getLife() {
        return this.entityData.get(DATA_LIFE);
    }
}
