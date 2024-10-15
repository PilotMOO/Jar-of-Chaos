package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.entities.projectiles.GrandPianoProjectile;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class PianoWand extends Item {
    public PianoWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player.level().isClientSide) return true;
        GrandPianoProjectile.SpawnPiano(player.isSecondaryUseActive() ? null : player, entity, 15);
        return true;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        GrandPianoProjectile.SpawnPiano(player == null || player.isSecondaryUseActive() ? null : context.getPlayer(), context.getClickLocation(), context.getLevel(), 15);
        return InteractionResult.SUCCESS;
    }
}
