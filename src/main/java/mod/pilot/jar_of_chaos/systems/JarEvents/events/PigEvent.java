package mod.pilot.jar_of_chaos.systems.JarEvents.events;

import mod.pilot.jar_of_chaos.systems.JarEvents.JarEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PigEvent extends JarEvent {
    public PigEvent(ServerLevel level, Entity parent){
        super("PigEvent", -1, level, parent, null);
    }
    public PigEvent(ServerLevel server, Vec3 pos) {
        super("PigEvent", -1, server, null, pos);
    }


    @Override
    public JarEvent Clone(@NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos) {
        if (parent != null){
            return new PigEvent(server, parent);
        }
        if (pos != null){
            return new PigEvent(server, pos);
        }
        return null;
    }

    @Override
    public void InstantEffect() {
        Pig pig = (Pig)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("minecraft", "pig")).create(server);
        pig.moveTo(getPosition());
        server.addFreshEntity(pig);
        if (getParent() != null && getParent() instanceof Player player){
            player.displayClientMessage(Component.literal("Oink"), false);
        }
    }
    @Override
    public void StartFlag() {}
    @Override
    public void EventTick() {}
    @Override
    public void FinalizeFlag() {}
}
