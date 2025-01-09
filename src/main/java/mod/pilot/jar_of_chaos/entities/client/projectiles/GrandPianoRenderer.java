package mod.pilot.jar_of_chaos.entities.client.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.pilot.jar_of_chaos.entities.projectiles.GrandPianoProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class GrandPianoRenderer extends GeoEntityRenderer<GrandPianoProjectile> {
    public GrandPianoRenderer(EntityRendererProvider.Context renderManager){
        super(renderManager, new GrandPianoModel());
    }

    @Override
    public void render(GrandPianoProjectile entity, float entityYaw, float partialTick, PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
