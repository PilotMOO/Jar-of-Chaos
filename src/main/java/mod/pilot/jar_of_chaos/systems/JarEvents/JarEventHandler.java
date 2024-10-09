package mod.pilot.jar_of_chaos.systems.JarEvents;

import mod.pilot.jar_of_chaos.systems.JarEvents.events.DisplacedContinuousExplosionEvent;
import mod.pilot.jar_of_chaos.systems.JarEvents.events.LotsOfPigsEvent;
import mod.pilot.jar_of_chaos.systems.JarEvents.events.PigEvent;
import mod.pilot.jar_of_chaos.systems.JarEvents.events.RandomExplode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class JarEventHandler {
    private static final RandomSource random = RandomSource.create();

    private static final ArrayList<JarEvent> activeEvents = new ArrayList<>();
    public static ArrayList<JarEvent> getEvents(){
        return new ArrayList<>(activeEvents);
    }
    public static void AddToEvents(JarEvent event){
        activeEvents.add(event);
    }
    public static void TickAllEvents(){
        for (JarEvent event : getEvents()){
            event.EventLifecycle();
        }
    }
    public boolean AmISubscribed(JarEvent event){
        for (JarEvent active : getEvents()){
            if (active == event) return true;
        }
        return false;
    }
    public static void CleanActiveEvents(){
        ArrayList<JarEvent> toRemove = new ArrayList<>();
        for (JarEvent event : getEvents()){
            if (event == null || event.isExpired()){
                toRemove.add(event);
                System.out.println("Removing " + event + " from ActiveEvents");
            }
        }

        activeEvents.removeAll(toRemove);
    }

    private static final ArrayList<JarEvent> EventPool = new ArrayList<>();
    public static void AddToEventPool(JarEvent event){
        EventPool.add(event);
    }
    public static ArrayList<JarEvent> getCopyOfEventPool(){
        return new ArrayList<>(EventPool);
    }
    public static @Nullable JarEvent getCloneFromEventPool(int index, @NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos){
        if (index > EventPool.size()) return null;
        return EventPool.get(index).Clone(server, parent, pos);
    }
    public static @Nullable JarEvent getRandomCloneFromEventPool(@NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos){
        return getCloneFromEventPool(random.nextInt(EventPool.size()), server, parent, pos);
    }
    public static @Nullable JarEvent getRandomCloneFromEventPool(int bound, @NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos){
        return getRandomCloneFromEventPool(0, bound, server, parent, pos);
    }
    public static @Nullable JarEvent getRandomCloneFromEventPool(int range, int bound, @NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos){
        return getCloneFromEventPool(random.nextIntBetweenInclusive(range, bound), server, parent, pos);
    }

    public static void PopulateEventPool(ServerLevel server){
        AddToEventPool(new RandomExplode(server, (Entity)null));
        AddToEventPool(new DisplacedContinuousExplosionEvent(600, server, null, null, 15));
        AddToEventPool(new PigEvent(server, (Entity)null));
        AddToEventPool(new LotsOfPigsEvent(server, (Entity)null, 10, 6));
    }
}
