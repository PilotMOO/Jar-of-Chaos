package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.entities.projectiles.SlimeBallProjectile;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class slimeballWand extends Item {
    public slimeballWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        SlimeBallProjectile.createAt(pLevel, pPlayer.position(), null, 4);
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
