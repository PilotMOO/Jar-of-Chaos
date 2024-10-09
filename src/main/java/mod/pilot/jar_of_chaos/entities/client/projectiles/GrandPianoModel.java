package mod.pilot.jar_of_chaos.entities.client.projectiles;

import mod.azure.azurelib.model.GeoModel;
import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.projectiles.GrandPianoProjectile;
import net.minecraft.resources.ResourceLocation;

public class GrandPianoModel extends GeoModel<GrandPianoProjectile> {
    private static final ResourceLocation model = new ResourceLocation(JarOfChaos.MOD_ID, "geo/entity/projectile/piano.geo.json");
    private static final ResourceLocation texture = new ResourceLocation(JarOfChaos.MOD_ID, "textures/entity/projectile/piano_texture.png");
    private static final ResourceLocation animation = new ResourceLocation(JarOfChaos.MOD_ID, "animations/entity/projectile/piano.animation.json");


    @Override
    public ResourceLocation getModelResource(GrandPianoProjectile animatable) {
        return model;
    }
    @Override
    public ResourceLocation getTextureResource(GrandPianoProjectile animatable) {
        return texture;
    }
    @Override
    public ResourceLocation getAnimationResource(GrandPianoProjectile animatable) {
        return animation;
    }
}
