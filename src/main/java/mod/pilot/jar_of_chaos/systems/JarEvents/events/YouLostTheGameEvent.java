package mod.pilot.jar_of_chaos.systems.JarEvents.events;

import mod.pilot.jar_of_chaos.systems.JarEvents.JarEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YouLostTheGameEvent extends JarEvent {
    public YouLostTheGameEvent(ServerLevel level, Entity parent){
        super("YouLostTheGameEvent", -1, level, parent, null);
    }

    @Override
    public JarEvent Clone(@NotNull ServerLevel server, @Nullable Entity parent, @Nullable Vec3 pos) {
        if (parent != null){
            return new YouLostTheGameEvent(server, parent);
        }
        return null;
    }

    @Override
    public void InstantEffect() {
        Vec3 pos = getPosition();
        if (pos == null) return;

        ItemEntity item = new ItemEntity(server, pos.x, pos.y, pos.z, generateItem());
        server.addFreshEntity(item);
        server.playSound(null, BlockPos.containing(pos), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 2f, 1f);
    }

    private static ItemStack generateItem() {
        return new ItemStack(Items.PAPER).setHoverName(Component.translatable("item.jar_of_chaos.jar_event.game_lost"));
    }

    @Override
    public void StartFlag() {}
    @Override
    public void EventTick() {}
    @Override
    public void FinalizeFlag() {}
}
