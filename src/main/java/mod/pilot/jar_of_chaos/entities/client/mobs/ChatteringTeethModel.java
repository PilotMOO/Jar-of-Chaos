package mod.pilot.jar_of_chaos.entities.client.mobs;

import mod.azure.azurelib.model.GeoModel;
import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.mobs.ChatteringTeethEntity;
import mod.pilot.jar_of_chaos.entities.projectiles.GrandPianoProjectile;
import net.minecraft.resources.ResourceLocation;

public class ChatteringTeethModel extends GeoModel<ChatteringTeethEntity> {
    private static final ResourceLocation model = new ResourceLocation(JarOfChaos.MOD_ID, "geo/entity/mob/chatteringteeth.geo.json");
    private static final ResourceLocation texture = new ResourceLocation(JarOfChaos.MOD_ID, "textures/entity/mob/chatteringteeth_texture.png");
    private static final ResourceLocation animation = new ResourceLocation(JarOfChaos.MOD_ID, "animations/entity/mob/chatteringteeth.animation.json");


    @Override
    public ResourceLocation getModelResource(ChatteringTeethEntity animatable) {
        return model;
    }
    @Override
    public ResourceLocation getTextureResource(ChatteringTeethEntity animatable) {
        return texture;
    }
    @Override
    public ResourceLocation getAnimationResource(ChatteringTeethEntity animatable) {
        return animation;
    }
}
