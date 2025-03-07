package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.entities.misc.SpecialItemEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MagicStickWand extends Item {
    public MagicStickWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        SpecialItemEntity sItem = SpecialItemEntity.CreateMagicStick(pLevel, pPlayer.position().add(0, 2, 0));
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
