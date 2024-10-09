package mod.pilot.jar_of_chaos.systems.JarEvents.events;

import mod.pilot.jar_of_chaos.systems.JarEvents.JarEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DisplacedContinuousExplosionEvent extends JarEvent {
    /**
     * When creating a new Event, make sure to call {@code JarEvent.Subscribe(YourEventHere)} to get it to register inside of Active Events,
     * otherwise it won't ever trigger! Instant Events do not require to be subscribed to take effect,
     * denote an instant event by inputting -1 for the duration.<p>
     * Check out IEventPersistent for creating new events that get saved in the world data
     * if you don't want them to be cleared after a relog.
     *
     * @param duration How long the effect lasts. -1 denotes an "instant" event
     * @param server The ServerLevel for use in calling effects on the server level. server.explode() is an example
     * @param parent the (Nullable) parent of the event, I.E. what entity called it (likely the player)
     * @param pos the (Nullable) position of the event, gets assigned the position of the parent if this argument is null and the parent argument isn't.
     */
    public DisplacedContinuousExplosionEvent(int duration, @NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos, int range) {
        super("DisplacedContinuousExplosion", duration, server, parent, pos);
        this.range = range;
        random = server.getRandom();
    }

    @Override
    public JarEvent Clone(@NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos) {
        return new DisplacedContinuousExplosionEvent(getMaxDuration(), server, parent, pos, range);
    }

    private final int range;
    private final RandomSource random;
    private int NextExplosionTracker;

    @Override
    public void InstantEffect() {

    }

    @Override
    public void StartFlag() {
        System.out.println("StartFlag was called!");
        server.explode(getParent(), null, new ExplosionDamageCalculator(), getRandomPosNearby(),
                random.nextIntBetweenInclusive(1, 5), false, Level.ExplosionInteraction.NONE);
        NextExplosionTracker = random.nextInt(20, 120);
    }

    @Override
    public void EventTick() {
        System.out.println("EventTick was called! ElapsedDuration: " + getElapsedDuration() + ", MaxDuration: " + getMaxDuration());
        if (getElapsedDuration() % NextExplosionTracker == 0){
            server.explode(getParent(), null, new ExplosionDamageCalculator(), getRandomPosNearby(),
                    random.nextIntBetweenInclusive(1, 5), false, Level.ExplosionInteraction.BLOCK);
            NextExplosionTracker = random.nextInt(20, 120);
        }
    }

    @Override
    public void FinalizeFlag() {
        System.out.println("FinalizeFlag was called!");
        assert getPosition() != null;
        server.explode(getParent(), null, new ExplosionDamageCalculator(), getPosition(),
                6, true, Level.ExplosionInteraction.BLOCK);
    }

    private Vec3 getRandomPosNearby(){
        assert getPosition() != null;
        return getPosition().add(random.nextIntBetweenInclusive(-range, range), random.nextIntBetweenInclusive(-range, range) ,random.nextIntBetweenInclusive(-range, range));
    }
}
