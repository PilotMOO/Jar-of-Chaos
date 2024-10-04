package mod.pilot.jar_of_chaos.items.custom.client;

import mod.azure.azurelib.model.GeoModel;
import mod.pilot.jar_of_chaos.items.custom.JarItem;
import net.minecraft.resources.ResourceLocation;

public class JarModel extends GeoModel<JarItem> {
    private static final ResourceLocation model = new ResourceLocation("jar_of_chaos", "geo/item/jar.geo.json");
    private static final ResourceLocation texture = new ResourceLocation("jar_of_chaos", "textures/item/jar.png");
    private static final ResourceLocation animation = new ResourceLocation("jar_of_chaos", "animations/item/jar.animation.json");


    @Override
    public ResourceLocation getModelResource(JarItem animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(JarItem animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(JarItem animatable) {
        return animation;
    }
}
