package mod.pilot.jar_of_chaos.entities.client.projectiles;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.projectiles.SlimeArrowProjectile;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SlimeArrowRenderer extends ArrowRenderer<SlimeArrowProjectile> {
    public SlimeArrowRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    private static final ResourceLocation texture = new ResourceLocation(JarOfChaos.MOD_ID, "textures/entity/projectiles/slime_arrow.png");
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull SlimeArrowProjectile pEntity) {
        return texture;
    }
}
