package mod.pilot.jar_of_chaos.entities.client.mobs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.pilot.jar_of_chaos.entities.mobs.ChatteringTeethEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public class ChatteringTeethRenderer extends GeoEntityRenderer<ChatteringTeethEntity> {
    public ChatteringTeethRenderer(EntityRendererProvider.Context renderManager){
        super(renderManager, new ChatteringTeethModel());
    }

    @Override
    public void render(@NotNull ChatteringTeethEntity teeth, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(teeth.getZLatchRotate()));

        super.render(teeth, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
