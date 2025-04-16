package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.damagetypes.JarDamageTypes;
import mod.pilot.jar_of_chaos.sound.JarSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToasterItem extends Item {
    public ToasterItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack itemStack, @NotNull Level level, Entity entity, int pSlotId, boolean pIsSelected) {
        if (entity.isInFluidType()){
            AABB affected = entity.getBoundingBox().inflate(7.5);
            for (Entity e : level.getEntities(entity, affected)){
                zap(e);
            }
            if (!(entity instanceof Player player) || !player.isSpectator() && !player.isCreative()) {
                zap(entity);
            }
        }
    }

    private void zap(Entity entity){
        if (entity instanceof LivingEntity le && le.isInFluidType() && le.invulnerableTime == 0){
            le.hurt(JarDamageTypes.toaster(le), 10);
            le.invulnerableTime = 5;
            le.level().playSound(null, le.blockPosition(), JarSounds.ZAP.get(), SoundSource.PLAYERS, 1f, 1f);
            /*le.playSound(JarSounds.ZAP.get());
            if (entity instanceof Player player){
                player.playSound(JarSounds.ZAP.get());
            }*/
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.jar_of_chaos.toaster.tooltip"));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
