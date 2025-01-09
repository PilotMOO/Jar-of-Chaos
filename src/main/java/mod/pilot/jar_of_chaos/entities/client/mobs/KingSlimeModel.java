package mod.pilot.jar_of_chaos.entities.client.mobs;

import mod.azure.azurelib.model.GeoModel;
import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.mobs.ChatteringTeethEntity;
import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;
import net.minecraft.resources.ResourceLocation;

public class KingSlimeModel extends GeoModel<KingSlimeEntity> {
    private static final ResourceLocation model = new ResourceLocation(JarOfChaos.MOD_ID, "geo/entity/mob/king_slime.geo.json");
    private static final ResourceLocation texture = new ResourceLocation(JarOfChaos.MOD_ID, "textures/entity/mob/king_slime_texture.png");
    private static final ResourceLocation outer_body_texture = new ResourceLocation(JarOfChaos.MOD_ID, "textures/entity/mob/king_slime_outer.png");
    private static final ResourceLocation animation = new ResourceLocation(JarOfChaos.MOD_ID, "animations/entity/mob/king_slime.animation.json");


    @Override
    public ResourceLocation getModelResource(KingSlimeEntity animatable) {
        return model;
    }
    @Override
    public ResourceLocation getTextureResource(KingSlimeEntity animatable) {
        return texture;
    }
    public static ResourceLocation getOuterTextureResource() {
        return outer_body_texture;
    }
    @Override
    public ResourceLocation getAnimationResource(KingSlimeEntity animatable) {
        return animation;
    }
}
