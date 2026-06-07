package net.hellomouse.createrailgun.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.hellomouse.createrailgun.Config;
import net.hellomouse.createrailgun.CreateRailgun;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = CreateRailgun.MODID, value = Dist.CLIENT)
public class TrailManager {
    private static final List<Trail> trails = new ArrayList<>();

    public static void addTrail(Trail trail) {
        if (trails.stream().noneMatch(t -> t.getId() == trail.getId()))
            trails.add(trail);
    }

    public static void markTrailExploded(int id) {
        for (Trail trail : trails)
            if (trail.getId() == id)
                trail.markExploded();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null) {
            trails.clear();
            return;
        }

        trails.removeIf(Trail::isDead);
        for (Trail trail : trails)
            trail.tick(Minecraft.getInstance().level);
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        trails.removeIf(Trail::isDead);
        if (trails.isEmpty()) return;

        // Pre-filter trails that are actually long enough to be drawn
        float partialTicks = event.getPartialTick().getGameTimeDeltaTicks();
        List<Trail> renderableTrails = trails.stream().filter(trail -> {
            Vec3 end = trail.getPrevEndPos().lerp(trail.getEndPos(), partialTicks);
            return end.subtract(trail.getStartPos()).lengthSqr() >= 0.001;
        }).toList();

        if (renderableTrails.isEmpty())
            return;

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();

        // Bloom
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f pose = poseStack.last().pose();

        for (Trail trail : renderableTrails) {
            Vec3 start = trail.getStartPos();
            Vec3 end = trail.getPrevEndPos().lerp(trail.getEndPos(), partialTicks);
            Vec3 dir = end.subtract(start).normalize();

            // Fade logic
            float lifeRatio = (float) trail.getLife() / Trail.MAX_LIFE;
            float startRatio = Math.max(0, (lifeRatio - 0.5f) * 2f);
            lifeRatio = 1.0f - (float)Math.pow(1.0f - lifeRatio, 3);

            // Geometry Math
            Vec3 mid = start.add(end).scale(0.5);
            Vec3 toCamera = cameraPos.subtract(mid).normalize();
            Vec3 rightDir = dir.cross(toCamera).normalize();

            // Bloom halo
            float haloThickness = 0.4f;
            int hR = 255, hG = 90, hB = 0;
            double trailOpacity = Config.TRAIL_OPACITY.getAsDouble() * 255;
            int hStartAlpha = (int)(trailOpacity * 0.53333333333 * startRatio);
            int hEndAlpha = (int)(trailOpacity * lifeRatio);

            Vec3 hRight = rightDir.scale(haloThickness);
            Vec3 h1 = start.subtract(hRight), h2 = start.add(hRight);
            Vec3 h3 = end.add(hRight), h4 = end.subtract(hRight);

            builder.addVertex(pose, (float)h1.x, (float)h1.y, (float)h1.z).setColor(hR, hG, hB, hStartAlpha);
            builder.addVertex(pose, (float)h2.x, (float)h2.y, (float)h2.z).setColor(hR, hG, hB, hStartAlpha);
            builder.addVertex(pose, (float)h3.x, (float)h3.y, (float)h3.z).setColor(hR, hG, hB, hEndAlpha);
            builder.addVertex(pose, (float)h4.x, (float)h4.y, (float)h4.z).setColor(hR, hG, hB, hEndAlpha);

            // Bloom core
            float coreThickness = 0.1f;
            int cR = 255, cG = 230, cB = 150;
            int cStartAlpha = (int)(255 * startRatio);
            int cEndAlpha = (int)(255 * lifeRatio);

            Vec3 cRight = rightDir.scale(coreThickness);
            Vec3 c1 = start.subtract(cRight), c2 = start.add(cRight);
            Vec3 c3 = end.add(cRight), c4 = end.subtract(cRight);

            builder.addVertex(pose, (float)c1.x, (float)c1.y - 0.001f, (float)c1.z).setColor(cR, cG, cB, cStartAlpha);
            builder.addVertex(pose, (float)c2.x, (float)c2.y - 0.001f, (float)c2.z).setColor(cR, cG, cB, cStartAlpha);
            builder.addVertex(pose, (float)c3.x, (float)c3.y - 0.001f, (float)c3.z).setColor(cR, cG, cB, cEndAlpha);
            builder.addVertex(pose, (float)c4.x, (float)c4.y - 0.001f, (float)c4.z).setColor(cR, cG, cB, cEndAlpha);

            builder.addVertex(pose, (float)c1.x, (float)c1.y + 0.001f, (float)c1.z).setColor(cR, cG, cB, cStartAlpha);
            builder.addVertex(pose, (float)c2.x, (float)c2.y + 0.001f, (float)c2.z).setColor(cR, cG, cB, cStartAlpha);
            builder.addVertex(pose, (float)c3.x, (float)c3.y + 0.001f, (float)c3.z).setColor(cR, cG, cB, cEndAlpha);
            builder.addVertex(pose, (float)c4.x, (float)c4.y + 0.001f, (float)c4.z).setColor(cR, cG, cB, cEndAlpha);
        }

        poseStack.popPose();
        BufferUploader.drawWithShader(builder.buildOrThrow());

        // Restore
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}