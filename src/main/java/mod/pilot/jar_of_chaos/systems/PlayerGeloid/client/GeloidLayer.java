package mod.pilot.jar_of_chaos.systems.PlayerGeloid.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.pilot.jar_of_chaos.systems.PlayerGeloid.GeloidManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class GeloidLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    public final RenderLayerParent<T, M> parent;
    public final AbstractGeloidModel<T> geloidModel;
    public GeloidLayer(RenderLayerParent<T, M> pRenderer, ModelPart root, boolean slim) {
        super(pRenderer);
        parent = pRenderer;
        geloidModel = slim ? new SlimGeloidModel<>(root) : new GeloidModel<>(root);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, @NotNull T parentEntity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (GeloidManager.isActiveGeloid(parentEntity)){
            getParentModel().copyPropertiesTo(geloidModel);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(geloidModel.getTextureResourceLocation(parentEntity)));
            geloidModel.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }
    }
}
