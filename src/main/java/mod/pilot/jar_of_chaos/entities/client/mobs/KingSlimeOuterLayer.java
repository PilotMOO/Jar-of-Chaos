package mod.pilot.jar_of_chaos.entities.client.mobs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.renderer.GeoRenderer;
import mod.azure.azurelib.renderer.layer.GeoRenderLayer;
import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

public class KingSlimeOuterLayer extends GeoRenderLayer<KingSlimeEntity> {
    public KingSlimeOuterLayer(GeoRenderer<KingSlimeEntity> entityRenderer) {
        super(entityRenderer);
    }


    @Override
    public void render(PoseStack poseStack, KingSlimeEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(KingSlimeModel.getOuterTextureResource()));

        // Render the model with the translucent layer
        getRenderer().actuallyRender(
                poseStack,
                animatable,
                bakedModel,
                renderType,
                bufferSource,
                vertexConsumer,
                false,
                partialTick,
                packedLight,
                packedOverlay,
                1.0f, 1.0f, 1.0f, 1.0f // RGBA, where A is the alpha/transparency
        );
    }
}