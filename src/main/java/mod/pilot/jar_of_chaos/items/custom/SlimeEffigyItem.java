package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.Config;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import mod.pilot.jar_of_chaos.systems.SlimeRain.SlimeRainManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlimeEffigyItem extends Item {
    public SlimeEffigyItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        DimensionType dType = player.level().dimensionType();
        if (!JarGeneralSaveData.isSlimeRain() && !dType.hasCeiling() && dType.hasSkyLight()){
            if (level instanceof ServerLevel server){
                SlimeRainManager.StartSlimeRain(server,
                        player.getRandom().nextInt(Config.SERVER.slime_rain_min_duration.get(), Config.SERVER.slime_rain_max_duration.get()),
                        false);

                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }

            player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.get(),
                    SoundSource.HOSTILE, 2f, 0.5f);
            player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                    SoundSource.HOSTILE, 0.25f, 0.75f);

            stack.shrink(1);

            Minecraft.getInstance().gameRenderer.displayItemActivation(stack);

            return new InteractionResultHolder<>(InteractionResult.CONSUME, stack);
        } else {
            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK,
                    SoundSource.HOSTILE, 1.5f, 0.75f);
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.jar_of_chaos.slime_effigy.tooltip"));
        super.appendHoverText(stack, pLevel, tooltip, flag);
    }
}
