package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.systems.JarEvents.JarEvent;
import mod.pilot.jar_of_chaos.systems.JarEvents.events.RandomExplode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ExplodeEventWandTest extends Item {
    public ExplodeEventWandTest(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level instanceof ServerLevel server){
            JarEvent.Subscribe(new RandomExplode(server, player));
        }
        return InteractionResultHolder.success(new ItemStack(this));
    }
}
