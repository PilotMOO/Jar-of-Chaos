package mod.pilot.jar_of_chaos.items.custom.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.azure.azurelib.renderer.GeoItemRenderer;
import mod.pilot.jar_of_chaos.items.custom.ChatterCannonItem;
import mod.pilot.jar_of_chaos.items.custom.JarItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

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
