package mod.pilot.jar_of_chaos.systems.JarEvents.events;

import mod.pilot.jar_of_chaos.systems.JarEvents.JarEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RandomExplode extends JarEvent {
    public RandomExplode(ServerLevel level, Entity parent){
        super("ExplodeEvent", -1, level, parent, null);
        System.out.println("Creating a new RandomExplodeEvent");
    }
    public RandomExplode(ServerLevel server, Vec3 pos) {
        super("ExplodeEvent", -1, server, null, pos);
        System.out.println("Creating a new RandomExplodeEvent");
    }


    @Override
    public JarEvent Clone(@NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos) {
        if (parent != null){
            return new RandomExplode(server, parent);
        }
        if (pos != null){
            return new RandomExplode(server, pos);
        }
        return null;
    }

    @Override
    public void InstantEffect() {
        System.out.println("Trying to explode");
        server.explode(getParent(), null, new ExplosionDamageCalculator(), getPosition(), 4, true, Level.ExplosionInteraction.MOB);
    }

    @Override
    public void StartFlag() {}
    @Override
    public void EventTick() {}
    @Override
    public void FinalizeFlag() {}
}
