package mod.pilot.jar_of_chaos.systems.JarEvents;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class JarEvent {
    /**
     Abstract class for all Jar Events created by the Jar.<p>
     When creating a new Event, make sure to call {@code JarEvent.Subscribe(YourEventHere)} to get it to register inside of Active Events,
     otherwise it won't ever trigger! Instant Events do not require to be subscribed to take effect,
     denote an instant event by inputting -1 for the duration.<p>
     Check out IEventPersistent for creating new events that get saved in the world data
     if you don't want them to be cleared after a relog. <p>
     Note that some parameters are Nullable, but try to feed in values when calling them anyway due to some events requiring them to not be null.
     Better safe than sorry

     * @param eventID The String ID of an event. Remove this parameter from the constructor and feed in your own custom I.D. automatically in the
     *                super() when extending from this class
     * @param duration How long the effect lasts. -1 denotes an "instant" event
     * @param server The (NotNull) ServerLevel for use in calling effects on the server level. server.explode() is an example
     * @param parent the (Nullable) parent of the event, I.E. what entity called it (likely the player)
     * @param pos the (Nullable) position of the event, gets assigned the position of the parent if this argument is null and the parent argument isn't.
     */
    protected JarEvent(String eventID, int duration, @NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos){
        EventID = eventID;
        EventDuration = duration;
        this.server = server;

        boolean nullBool = pos == null;
        setParent(parent, nullBool);
        if (!nullBool){
            setPosition(pos);
        }
        EventState = (byte) (EventDuration == -1 ? 0 : 1);
    }
    public abstract JarEvent Clone(@NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos);
    public enum EventStates{
        Instant,
        Inactive,
        Starting,
        Active,
        Ending,
        Expired
    }

    @Override
    public String toString() {
        return getEventID() + " at " + getPosition();
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
        if (getState() == 0 && state != 5){
            System.out.println("You can't assign a new state to a Jar Event marked as Instant!");
            return;
        }
        EventState = state;
    }
    public void setState(EventStates state){
        setState((byte)state.ordinal());
    }
    public void Disable(){
        setState(EventStates.Inactive);
    }
    public void Start(){
        setState(EventStates.Starting);
    }
    public void Activate(){
        setState(EventStates.Active);
    }
    public void Finish(){
        setState(EventStates.Ending);
    }
    public void Expire(){
        setState(EventStates.Expired);
        JarEventHandler.CleanActiveEvents();
    }
    public boolean isInactive(){
        return getState() == 1;
    }
    public boolean isStarting(){
        return getState() == 2;
    }
    public boolean isActive(){
        return getState() == 3;
    }
    public boolean isEnding(){
        return getState() == 4;
    }
    public boolean isExpired(){
        return getState() == 5;
    }

    private final int EventDuration;
    private int elapsedDuration = 0;
    protected void TickTimer(){
        elapsedDuration++;
    }
    public int getElapsedDuration(){
        return elapsedDuration;
    }
    public int getMaxDuration(){
        return EventDuration;
    }
    public boolean isInstant(){
        return EventDuration == -1 || getState() == 0;
    }

    protected @NotNull ServerLevel server;

    private @Nullable Entity parentEntity;
    public @Nullable Entity getParent(){
        return parentEntity;
    }
    public void setParent(Entity parent, boolean setPosition){
        parentEntity = parent;
        if (setPosition && parent != null){
            setPosition(parent.position());
        }
    }
    public void setParent(Entity parent){
        setParent(parent, true);
    }
    private @Nullable Vec3 eventPosition;
    public @Nullable Vec3 getPosition(){
        return eventPosition;
    }
    public void setPosition(Vec3 pos){
        eventPosition = pos;
    }

    public static void Subscribe(JarEvent event){
        System.out.println("Subscribing Event " + event);
        JarEventHandler.AddToEvents(event);
        if (!event.isInstant()) {
            event.Start();
        }
    }

    /**Gets called every tick, manages which methods to call. Edit {@code InstantEffect()}, {@code StartFlag()}, {@code EventTick()}, and {@code FinalizeFlags()} for event effects, not this.*/
    public void EventLifecycle(){
        switch (getState()) {
            default -> {
                return;
            }
            case 0 ->{
                InstantEffect(); Expire();
            }
            case 2 -> {
                StartFlag(); Activate();
            }
            case 3 -> {
                EventTick(); if (getElapsedDuration() >= getMaxDuration()) Finish();
            }
            case 4 -> {
                FinalizeFlag(); Expire();
            }
        }
        TickTimer();
    }

    /**Gets called upon the first tick of the event if marked as "Instant", event expires after being called.*/
    public abstract void InstantEffect();
    /**Gets called upon the first tick of the event if it is NOT marked as "Instant". Used for start-of-event effects*/
    public abstract void StartFlag();
    /**Gets called every tick if the state is Active, used for middle of event effects and effects drawn out across multiple ticks*/
    public abstract void EventTick();
    /**Gets called right before the event expires, used to wrap up the event*/
    public abstract void FinalizeFlag();
}
