package mod.pilot.jar_of_chaos.events;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.entities.mobs.ChatteringTeethEntity;
import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JarOfChaos.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)

public class JarEventBus {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event){
        event.put(JarEntities.CHATTERING_TEETH.get(), ChatteringTeethEntity.createAttributes().build());
        event.put(JarEntities.KING_SLIME.get(), KingSlimeEntity.createAttributes().build());
    }
}
