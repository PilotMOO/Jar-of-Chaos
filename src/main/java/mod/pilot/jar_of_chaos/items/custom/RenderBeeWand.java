package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.systems.ModelDisplay.InWorldDisplayManager;
import mod.pilot.jar_of_chaos.systems.ModelDisplay.client.BeeModel;
import mod.pilot.jar_of_chaos.systems.ModelDisplay.client.GenericModelHub;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RenderBeeWand extends Item {
    public RenderBeeWand(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pLevel.isClientSide()){
            System.out.println("client invoke");
            InWorldDisplayManager.addRenderPackage(
                    new InWorldDisplayManager.RenderPackage(
                            new InWorldDisplayManager.ModelDisplay(
                                    new BeeModel(GenericModelHub.ModelSet.bakeLayer(BeeModel.LAYER_LOCATION))),
                            pPlayer.getEyePosition()));
        }
        System.out.println("invoke");
        return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
    }
}
