package mod.pilot.jar_of_chaos.items.custom.client;

import mod.azure.azurelib.renderer.GeoItemRenderer;
import mod.pilot.jar_of_chaos.items.custom.JarOfChaosItem;

public class JarOfChaosRenderer extends GeoItemRenderer<JarOfChaosItem> {
    public JarOfChaosRenderer() {
        super(new JarOfChaosModel());
        useNewOffset();
    }

    @Override
    public boolean useNewOffset() {
        return true;
    }
}
