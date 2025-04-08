package mod.pilot.jar_of_chaos.items.custom.client;

import mod.azure.azurelib.renderer.GeoItemRenderer;
import mod.pilot.jar_of_chaos.items.custom.JesterBowItem;

public class JesterBowRenderer extends GeoItemRenderer<JesterBowItem> {
    public JesterBowRenderer() {
        super(new JesterBowModel());
        useNewOffset();
    }

    @Override
    public boolean useNewOffset() {
        return true;
    }
}
