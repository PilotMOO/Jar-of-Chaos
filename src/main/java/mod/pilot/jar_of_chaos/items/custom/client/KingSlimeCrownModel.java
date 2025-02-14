package mod.pilot.jar_of_chaos.items.custom.client;

import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.items.custom.KingSlimeCrown;
import net.minecraft.resources.ResourceLocation;

public class KingSlimeCrownModel extends GeoModel<KingSlimeCrown> {
    private static final ResourceLocation model = new ResourceLocation(JarOfChaos.MOD_ID, "geo/item/king_slime_crown_armor.geo.json");
    private static final ResourceLocation texture = new ResourceLocation(JarOfChaos.MOD_ID, "textures/item/king_slime_crown_texture.png");
    private static final ResourceLocation animation = new ResourceLocation(JarOfChaos.MOD_ID, "animations/item/king_slime_crown.animation.json");
    @Override
    public ResourceLocation getModelResource(KingSlimeCrown animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(KingSlimeCrown animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(KingSlimeCrown animatable) {
        return animation;
    }

    @Override
    public void setCustomAnimations(KingSlimeCrown animatable, long instanceId, AnimationState<KingSlimeCrown> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
    }
}
