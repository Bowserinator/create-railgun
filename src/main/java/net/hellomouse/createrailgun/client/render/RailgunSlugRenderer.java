package net.hellomouse.createrailgun.client.render;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.hellomouse.createrailgun.CreateRailgun;
import net.hellomouse.createrailgun.client.entity.RailgunSlugModel;
import net.hellomouse.createrailgun.entity.RailgunSlugEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class RailgunSlugRenderer extends EntityRenderer<RailgunSlugEntity> {
    protected final RailgunSlugModel MODEL;

    public RailgunSlugRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        MODEL = new RailgunSlugModel(renderManager.bakeLayer(RailgunSlugModel.LAYER_LOCATION));
    }

    @Override
    public void render(RailgunSlugEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int lighting) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XN.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        poseStack.translate(0.0D, 1.5F, -0.15D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        float ageInTicks = entity.tickCount + partialTicks;
        RenderType renderType = RenderType.entityCutout(this.getTextureLocation(entity));
        VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);

        MODEL.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        MODEL.renderToBuffer(poseStack, vertexconsumer, 1, 1, 1);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, lighting);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(RailgunSlugEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(CreateRailgun.MODID, "textures/entity/railgun_shell.png");
    }
}
