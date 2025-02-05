package mod.pilot.jar_of_chaos.entities.client.projectiles;

import mod.azure.azurelib.model.GeoModel;
import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.projectiles.GrandPianoProjectile;
import mod.pilot.jar_of_chaos.entities.projectiles.JesterArrowProjectile;
import net.minecraft.resources.ResourceLocation;

public class JesterArrowModel extends GeoModel<JesterArrowProjectile> {
    private static final ResourceLocation model = new ResourceLocation(JarOfChaos.MOD_ID, "geo/entity/projectiles/jesterarrow.geo.json");
    private static final ResourceLocation texture = new ResourceLocation(JarOfChaos.MOD_ID, "textures/entity/projectiles/jesterarrow_texture.png");
    private static final ResourceLocation animation = new ResourceLocation(JarOfChaos.MOD_ID, "animations/entity/projectiles/jesterarrow.animation.json");


    @Override
    public ResourceLocation getModelResource(JesterArrowProjectile animatable) {
        return model;
    }
    @Override
    public ResourceLocation getTextureResource(JesterArrowProjectile animatable) {
        return texture;
    }
    @Override
    public ResourceLocation getAnimationResource(JesterArrowProjectile animatable) {
        return animation;
    }
}
