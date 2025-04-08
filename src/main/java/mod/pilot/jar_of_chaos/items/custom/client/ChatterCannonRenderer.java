package mod.pilot.jar_of_chaos.items.custom.client;

import mod.azure.azurelib.renderer.GeoItemRenderer;
import mod.pilot.jar_of_chaos.items.custom.ChatterCannonItem;

public class ChatterCannonRenderer extends GeoItemRenderer<ChatterCannonItem> {
    public ChatterCannonRenderer() {
        super(new ChatterCannonModel());
        useNewOffset();
    }

    @Override
    public boolean useNewOffset() {
        return true;
    }
}
