package mod.pilot.jar_of_chaos.items.custom.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.renderer.GeoArmorRenderer;
import mod.pilot.jar_of_chaos.items.custom.KingSlimeCrown;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public class KingSlimeCrownRenderer extends GeoArmorRenderer<KingSlimeCrown> {
    public KingSlimeCrownRenderer() {
        super(new KingSlimeCrownModel());
    }

    @Override
    public @Nullable GeoBone getHeadBone() {
        return this.head = model.getBone("Crown").orElse(null);
    }

    @Override
    public @Nullable GeoBone getBodyBone() {
        return this.body = model.getBone("Cape").orElse(null);
    }

    //We want the body to render with the crown, keeping it unoverridden would hide it :[
    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        setAllVisible(true);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, KingSlimeCrown animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
