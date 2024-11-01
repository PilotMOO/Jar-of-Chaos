package mod.pilot.jar_of_chaos.entities;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.mobs.ChatteringTeethEntity;
import mod.pilot.jar_of_chaos.entities.projectiles.GrandPianoProjectile;
import mod.pilot.jar_of_chaos.entities.projectiles.JesterArrowProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class JarEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JarOfChaos.MOD_ID);


    public static final RegistryObject<EntityType<ChatteringTeethEntity>> CHATTERING_TEETH =
            ENTITY_TYPES.register("chattering_teeth", () -> EntityType.Builder.of(ChatteringTeethEntity::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f).build("chattering_teeth"));

    public static final RegistryObject<EntityType<GrandPianoProjectile>> PIANO =
            ENTITY_TYPES.register("piano", () -> EntityType.Builder.of(GrandPianoProjectile::new, MobCategory.MISC)
                    .sized(4f, 2.5f).build("piano"));
    public static final RegistryObject<EntityType<JesterArrowProjectile>> JESTER_ARROW =
            ENTITY_TYPES.register("jester_arrow", () -> EntityType.Builder.of(JesterArrowProjectile::new, MobCategory.MISC)
                    .sized(0.2f, 0.2f).build("jester_arrow"));
    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }
}
