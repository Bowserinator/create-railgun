package net.hellomouse.createrailgun.munitions.big_cannon;

import net.hellomouse.createrailgun.entity.RailgunSlugEntity;
import net.hellomouse.createrailgun.registry.CREntities;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.CreateBigCannons;
import rbasamoyai.createbigcannons.munitions.ShellExplosion;
import rbasamoyai.createbigcannons.munitions.big_cannon.he_shell.HEShellProjectile;

public class BurstShellProjectile extends HEShellProjectile {
    public BurstShellProjectile(EntityType<? extends BurstShellProjectile> type, Level level) {
        super(type, level);
    }

    protected void detonate(Position position) {
        ShellExplosion explosion = new ShellExplosion(this.level(), this, this.indirectArtilleryFire(false), position.x(), position.y(), position.z(),
                this.getAllProperties().explosion().blockDamagePower(),
                this.getAllProperties().explosion().entityDamagePower(),
                false, Explosion.BlockInteraction.DESTROY);
        CreateBigCannons.handleCustomExplosion(this.level(), explosion);

        var level = this.level();
        if (level instanceof ServerLevel serverLevel) {
            final double RAILGUN_SPEED = 3;

            Vec3 forward = this.getDeltaMovement().normalize();
            Vec3 up = this.getUpVector(1.0f).normalize();
            Vec3 right = forward.cross(up).normalize();

            final double offsetAngle = 10;
            final double cosOffset = Math.cos(Math.toRadians(offsetAngle));
            final double sinOffset = Math.sin(Math.toRadians(offsetAngle));

            spawnSlug(serverLevel, forward.scale(RAILGUN_SPEED));
            Vec3 topDir = forward.scale(cosOffset).add(up.scale(sinOffset)).normalize();
            spawnSlug(serverLevel, topDir.scale(RAILGUN_SPEED));
            Vec3 bottomDir = forward.scale(cosOffset).subtract(up.scale(sinOffset)).normalize();
            spawnSlug(serverLevel, bottomDir.scale(RAILGUN_SPEED));
            Vec3 rightDir = forward.scale(cosOffset).add(right.scale(sinOffset)).normalize();
            spawnSlug(serverLevel, rightDir.scale(RAILGUN_SPEED));
            Vec3 leftDir = forward.scale(cosOffset).subtract(right.scale(sinOffset)).normalize();
            spawnSlug(serverLevel, leftDir.scale(RAILGUN_SPEED));
        }
    }

    private void spawnSlug(ServerLevel level, Vec3 velocity) {
        RailgunSlugEntity slug = CREntities.RAILGUN_PROJECTILE.get().create(level);
        if (slug != null) {
            final double OFFSET = 0.2;
            slug.moveTo(this.getX() + velocity.x * OFFSET, this.getEyeY() + velocity.y * OFFSET, this.getZ() + velocity.z * OFFSET);
            slug.setDeltaMovement(velocity);
            level.addFreshEntity(slug);
        }
    }
}
