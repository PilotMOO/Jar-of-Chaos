package mod.pilot.jar_of_chaos.systems.JarEvents;

import java.util.ArrayList;

public class JarEventHandler {
    private static final ArrayList<JarEvent> activeEvents = new ArrayList<>();
    public static ArrayList<JarEvent> getEvents(){
        return new ArrayList<>(activeEvents);
    }
    public static void AddToEvents(JarEvent event){
        activeEvents.add(event);
    }
    public boolean AmIActive(JarEvent event){
        for (JarEvent active : getEvents()){
            if (active == event) return true;
        }
        return false;
    }
}
