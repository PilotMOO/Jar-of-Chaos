package mod.pilot.jar_of_chaos.systems.JarEvents;

public abstract class JarEvent {
    protected JarEvent(String eventID, int duration){
        EventID = eventID;
        EventDuration = duration;
        if (isInstant()){
            EventState = 0;
        }
    }

    public enum EventStates{
        Instant,
        Inactive,
        Active,
        Ended
    }

    private final String EventID;
    public final String getEventID(){
        return EventID;
    }

    private byte EventState;
    public byte getState(){
        return EventState;
    }
    public void setState(byte state){
        if (getState() == 0){
            System.out.println("You can't assign a new state to a Jar Event marked as Instant!");
            return;
        }
        EventState = state;
    }
    public void setState(EventStates state){
        setState((byte)state.ordinal());
    }

    private final int EventDuration;
    private int elapsedDuration;
    public int getMaxDuration(){
        return EventDuration;
    }
    public boolean isInstant(){
        return EventDuration == -1 || getState() == 0;
    }
}
