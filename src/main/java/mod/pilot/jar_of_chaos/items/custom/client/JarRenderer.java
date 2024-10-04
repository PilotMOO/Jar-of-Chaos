package mod.pilot.jar_of_chaos.items.custom.client;

import mod.azure.azurelib.renderer.GeoItemRenderer;
import mod.pilot.jar_of_chaos.items.custom.JarItem;

public class JarRenderer extends GeoItemRenderer<JarItem> {
    public JarRenderer() {
        super(new JarModel());
        useNewOffset();
    }

    @Override
    public boolean useNewOffset() {
        return true;
    }
}
