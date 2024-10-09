package mod.pilot.jar_of_chaos.systems.JarEvents.events;

import mod.pilot.jar_of_chaos.systems.JarEvents.JarEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LotsOfPigsEvent extends JarEvent {
    public LotsOfPigsEvent(ServerLevel server, Entity parent, int radius, int height){
        super("LotsOfPigsEvent", -1, server, parent, null);
        this.radius = radius;
        this.height = height;
    }
    public LotsOfPigsEvent(ServerLevel server, Vec3 pos, int radius, int height) {
        super("LotsOfPigsEvent", -1, server, null, pos);
        this.radius = radius;
        this.height = height;
    }

    private final int radius;
    private final int height;


    @Override
    public JarEvent Clone(@NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos) {
        if (parent != null){
            return new LotsOfPigsEvent(server, parent, radius, height);
        }
        else if (pos != null){
            return new LotsOfPigsEvent(server, pos, radius, height);
        }
        return null;
    }

    @Override
    public void InstantEffect() {
        assert getPosition() != null;
        for (int x = -radius; x < radius; x++){
            for (int z = -radius; z < radius; z++){
                CreatePig(getPosition().add(x, height, z));
            }
        }
        if (getParent() != null && getParent() instanceof Player player){
            player.displayClientMessage(Component.literal("That's a lot of pigs...!"), false);
        }
    }

    @Override
    public void StartFlag() {}
    @Override
    public void EventTick() {}
    @Override
    public void FinalizeFlag() {}

    private void CreatePig(Vec3 pos){
        Pig pig = (Pig)ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("minecraft", "pig")).create(server);
        pig.moveTo(pos);
        server.addFreshEntity(pig);
    }
}
