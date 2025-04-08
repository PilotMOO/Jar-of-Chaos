package mod.pilot.jar_of_chaos.items.custom.client;

import mod.azure.azurelib.model.GeoModel;
import mod.pilot.jar_of_chaos.items.custom.ChatterCannonItem;
import net.minecraft.resources.ResourceLocation;

public class ChatterCannonModel extends GeoModel<ChatterCannonItem> {
    private static final ResourceLocation model = new ResourceLocation("jar_of_chaos", "geo/item/chattercannon.geo.json");
    private static final ResourceLocation texture = new ResourceLocation("jar_of_chaos", "textures/item/chattercannon.png");
    private static final ResourceLocation animation = new ResourceLocation("jar_of_chaos", "animations/item/chattercannon.animation.json");


    @Override
    public ResourceLocation getModelResource(ChatterCannonItem animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(ChatterCannonItem animatable) {
        return texture;
    }

    @Override
    public ResourceLocation getAnimationResource(ChatterCannonItem animatable) {
        return animation;
    }
}
