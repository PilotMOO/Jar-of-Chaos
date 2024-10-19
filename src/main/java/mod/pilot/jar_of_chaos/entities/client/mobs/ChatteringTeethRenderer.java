package mod.pilot.jar_of_chaos.entities.client.mobs;

import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.pilot.jar_of_chaos.entities.mobs.ChatteringTeethEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ChatteringTeethRenderer extends GeoEntityRenderer<ChatteringTeethEntity> {
    public ChatteringTeethRenderer(EntityRendererProvider.Context renderManager){
        super(renderManager, new ChatteringTeethModel());
    }
}
