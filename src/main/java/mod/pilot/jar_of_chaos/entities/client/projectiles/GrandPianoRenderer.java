package mod.pilot.jar_of_chaos.entities.client.projectiles;

import mod.azure.azurelib.model.GeoModel;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.pilot.jar_of_chaos.entities.projectiles.GrandPianoProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class GrandPianoRenderer extends GeoEntityRenderer<GrandPianoProjectile> {
    public GrandPianoRenderer(EntityRendererProvider.Context renderManager){
        super(renderManager, new GrandPianoModel());
    }
}
