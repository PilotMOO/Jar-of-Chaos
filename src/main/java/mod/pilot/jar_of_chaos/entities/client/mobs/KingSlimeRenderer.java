package mod.pilot.jar_of_chaos.entities.client.mobs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public class KingSlimeRenderer extends GeoEntityRenderer<KingSlimeEntity> {

    public KingSlimeRenderer(EntityRendererProvider.Context renderManager){
        super(renderManager, new KingSlimeModel());
        addRenderLayer(new KingSlimeOuterLayer(this));
    }

    @Override
    public void render(@NotNull KingSlimeEntity kSlime, float entityYaw, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.translate(0, 0.01f, 0);

        float scale = kSlime.getSizeScale();
        getGeoModel().getBone("Crown").ifPresent(crown -> {
            //crown.setPosY(1f + (kSlime.getSize() * 0.25f));
            crown.setScaleX(1.5f / scale);
            crown.setScaleY(1.5f / scale);
            crown.setScaleZ(1.5f / scale);
        });

        super.render(kSlime, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
