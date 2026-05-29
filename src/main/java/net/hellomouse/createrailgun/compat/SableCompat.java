package net.hellomouse.createrailgun.compat;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.lang.ref.WeakReference;

public class SableCompat {
    public static Vector3d getSableSubContrapationVelocity(Level level, BlockPos worldPosition) {
        Vector3d sableSubContrapationVelocity = new Vector3d(0);
        var subLevelReference = new WeakReference<>(Sable.HELPER.getContaining(level, worldPosition));
        final SubLevel subLevel = subLevelReference.get();
        if (!level.isClientSide && subLevel != null) {
            final Vector3d jomlPos = JOMLConversion.toJOML(worldPosition.getCenter());
            sableSubContrapationVelocity = subLevel.logicalPose().transformPosition(jomlPos, new Vector3d()).sub(subLevel.lastPose().transformPosition(jomlPos, new Vector3d()), jomlPos).mul(20.0F);
        }
        return sableSubContrapationVelocity;
    }

    public static float getDotWithFacing(Level level, BlockPos worldPosition, Direction facing, Vector3d sableSubContrapationVelocity) {
        var subLevelReference = new WeakReference<>(Sable.HELPER.getContaining(level, worldPosition));
        final SubLevel subLevel = subLevelReference.get();
        if (!level.isClientSide && subLevel != null) {
            var currentNormal = JOMLConversion.toJOML(Vec3.atLowerCornerOf(facing.getNormal()));
            return (float) sableSubContrapationVelocity.dot(subLevel.logicalPose().transformNormal(currentNormal, new Vector3d()));
        }
        return 0F;
    }
}
