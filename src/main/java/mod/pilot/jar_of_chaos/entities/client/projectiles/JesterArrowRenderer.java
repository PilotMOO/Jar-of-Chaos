package mod.pilot.jar_of_chaos.entities.client.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.pilot.jar_of_chaos.entities.projectiles.GrandPianoProjectile;
import mod.pilot.jar_of_chaos.entities.projectiles.JesterArrowProjectile;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Optional;

public class JesterArrowRenderer extends GeoEntityRenderer<JesterArrowProjectile> {
    public JesterArrowRenderer(EntityRendererProvider.Context renderManager){
        super(renderManager, new JesterArrowModel());
    }

    @Override
    public void render(@NotNull JesterArrowProjectile arrow, float yaw, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource pBuffer, int pPackedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, arrow.yRotO, arrow.getYRot())));
        poseStack.mulPose(Axis.XN.rotationDegrees(Mth.lerp(partialTick, arrow.xRotO, arrow.getXRot())));
        super.render(arrow, yaw, partialTick, poseStack, pBuffer, pPackedLight);
        poseStack.popPose();
    }
}
