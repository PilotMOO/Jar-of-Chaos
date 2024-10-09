package mod.pilot.jar_of_chaos.entities;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.projectiles.GrandPianoProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class JarEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JarOfChaos.MOD_ID);


    public static final RegistryObject<EntityType<GrandPianoProjectile>> PIANO =
            ENTITY_TYPES.register("piano", () -> EntityType.Builder.of(GrandPianoProjectile::new, MobCategory.MISC)
                    .sized(4f, 2.5f).build("piano"));
    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }
}
