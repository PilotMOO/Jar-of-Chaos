package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.systems.PlayerGeloid.GeloidManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GeloidSetter extends Item {
    public GeloidSetter(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (GeloidManager.isActiveGeloid(pPlayer) && false){
            GeloidManager.removePlayerFromGeloid(pPlayer);
            pPlayer.displayClientMessage(Component.literal("Removing you from geloid list!"), true);
        } else {
            GeloidManager.addPlayerAsGeloid(pPlayer);
            pPlayer.displayClientMessage(Component.literal("adding you to geloid list!"), true);
        }
        pPlayer.getCooldowns().addCooldown(this, 2);
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
