package mod.pilot.jar_of_chaos.systems.JesterArrowEvents;

import mod.pilot.jar_of_chaos.entities.projectiles.JesterArrowProjectile;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.ArrayList;


public class JesterArrowEventManager {
    public static void Setup(){
        MinecraftForge.EVENT_BUS.addListener(JesterArrowEventManager::JesterArrowEventTracker);
        RegisterAllEvents();
    }
    public static void RegisterAllEvents(){
        JesterArrowEvent.CombustEvent.Register();
        JesterArrowEvent.VolleyEvent.Register();
        JesterArrowEvent.FishEvent.Register();
        JesterArrowEvent.TridentEvent.Register();
        JesterArrowEvent.FreezeEvent.Register();
        JesterArrowEvent.LightningEvent.Register();
    }

    private static final RandomSource random = RandomSource.create();
    private static final ArrayList<JesterArrowEvent> Events = new ArrayList<>();
    public static void RegisterEvent(JesterArrowEvent event){
        Events.add(event);
    }
    public static int EventCount(){
        return Events.size();
    }

    public static Pair<JesterArrowEvent, Integer> createNewRandomEvent(JesterArrowProjectile parent,
                                                                                @Nullable ArrayList<Class<? extends JesterArrowEvent>> AvoidClasses){
        int index;

        if (AvoidClasses != null){
            ArrayList<JesterArrowEvent> Whitelisted = new ArrayList<>(Events);
            ArrayList<JesterArrowEvent> Blacklisted = new ArrayList<>();
            for (JesterArrowEvent event : Whitelisted) {
                if (AvoidClasses.contains(event.getClass())) {
                    Blacklisted.add(event);
                }
            }
            Whitelisted.removeAll(Blacklisted);
            boolean whitelistFlag = Whitelisted.size() > 0;
            index = random.nextInt(whitelistFlag ? Whitelisted.size() : EventCount());
            if (!whitelistFlag) return new Pair<>(Events.get(index).Create(parent), index);
            return new Pair<>(Whitelisted.get(index).Create(parent), index);
        }
        index = random.nextInt(EventCount());
        return new Pair<>(Events.get(index).Create(parent), index);
    }
    public @Nullable static JesterArrowEvent createNewEventFromIndex(int index, JesterArrowProjectile parent){
        if (index == -1 || index > EventCount()) return null;

        return Events.get(index).Create(parent);
    }

    public static void JesterArrowEventTracker(EntityJoinLevelEvent event){
        if (event.getLevel().getServer() == null || !event.getLevel().getServer().isReady()) return;
        Entity E = event.getEntity();
        if (E instanceof JesterArrowProjectile J && J.Event != null && !J.getEventFired()){
            J.Event.OnSpawn();
        }
    }
}
