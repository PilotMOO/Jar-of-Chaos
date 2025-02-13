package mod.pilot.jar_of_chaos.systems.PlayerSlimeoid.client;

import mod.pilot.jar_of_chaos.JarOfChaos;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractSlimeoidModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation(JarOfChaos.MOD_ID, "textures/entity/slimeoid_texture.png");
    public ResourceLocation getTextureResourceLocation(T entity){
        return DEFAULT_TEXTURE_LOCATION;
    }
    public AbstractSlimeoidModel(ModelPart pRoot) {
        super(pRoot);
    }
}
