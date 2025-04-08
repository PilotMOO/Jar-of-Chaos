package mod.pilot.jar_of_chaos.items.custom.client;

import mod.azure.azurelib.model.GeoModel;
import mod.pilot.jar_of_chaos.items.custom.JesterBowItem;
import net.minecraft.resources.ResourceLocation;

public class JesterBowModel extends GeoModel<JesterBowItem> {
    private static final ResourceLocation model = new ResourceLocation("jar_of_chaos", "geo/item/jesterbow.geo.json");
    private static final ResourceLocation texture = new ResourceLocation("jar_of_chaos", "textures/item/jesterbow_texture.png");
    private static final ResourceLocation animation = new ResourceLocation("jar_of_chaos", "animations/item/jesterbow.animation.json");


    @Override
    public ResourceLocation getModelResource(JesterBowItem animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(JesterBowItem animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(JesterBowItem animatable) {
        return animation;
    }
}
