package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.systems.PlayerSlimeoid.SlimeoidManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SlimeoidSetter extends Item {
    public SlimeoidSetter(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (SlimeoidManager.isActiveSlimeoid(pPlayer) && false){
            SlimeoidManager.removePlayerFromSlimeoid(pPlayer);
            pPlayer.displayClientMessage(Component.literal("Removing you from slimeoid list!"), true);
        } else {
            SlimeoidManager.addPlayerAsSlimeoid(pPlayer);
            pPlayer.displayClientMessage(Component.literal("adding you to slimeoid list!"), true);
        }
        pPlayer.getCooldowns().addCooldown(this, 2);
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
