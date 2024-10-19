package mod.pilot.jar_of_chaos.events;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.projectiles.JesterArrowProjectile;
import mod.pilot.jar_of_chaos.systems.JarEvents.JarEventHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = JarOfChaos.MOD_ID)
public class JarForgeEventHandler {
    private static ServerLevel Overworld;
    public static @NotNull ServerLevel getServer(){
        return Overworld;
    }

    @SubscribeEvent
    public static void GetServerFromStarting(ServerStartingEvent event){
        Overworld = event.getServer().overworld();
        JarEventHandler.PopulateEventPool(Overworld);
    }

    @SubscribeEvent
    public static void ClearEvents(ServerStoppedEvent event){
        JarEventHandler.PurgeAllEvents();
        System.out.println("Clearing out all Events");
    }

    @SubscribeEvent
    public static void JarEventTicker(TickEvent.ServerTickEvent event){
        JarEventHandler.TickAllEvents();
    }

    @SubscribeEvent
    public static void JesterArrowEventTracker(EntityJoinLevelEvent event){
        if (event.getLevel().getServer() == null || !event.getLevel().getServer().isReady()) return;
        Entity E = event.getEntity();
        if (E instanceof JesterArrowProjectile J && J.Event != null && !J.getEventFired()){
            J.Event.OnSpawn();
        }
    }
}
