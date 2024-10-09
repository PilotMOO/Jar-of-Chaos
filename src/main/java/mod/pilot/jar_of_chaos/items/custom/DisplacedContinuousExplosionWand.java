package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.systems.JarEvents.JarEvent;
import mod.pilot.jar_of_chaos.systems.JarEvents.events.DisplacedContinuousExplosionEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DisplacedContinuousExplosionWand extends Item {
    public DisplacedContinuousExplosionWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level instanceof ServerLevel server){
            System.out.println("Wand is trying to make a new DisplacedContinuousExplosion Event");
            JarEvent.Subscribe(new DisplacedContinuousExplosionEvent(400, server, player, null, 10));
        }
        return InteractionResultHolder.success(new ItemStack(this));
    }
}
