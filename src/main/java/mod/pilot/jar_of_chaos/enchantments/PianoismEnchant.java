package mod.pilot.jar_of_chaos.enchantments;

import mod.pilot.jar_of_chaos.Config;
import mod.pilot.jar_of_chaos.entities.projectiles.GrandPianoProjectile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PianoismEnchant extends Enchantment {
    protected PianoismEnchant() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, Arrays.asList(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND).toArray(new EquipmentSlot[2]));
    }

    private static final double BasePianoChance = Config.SERVER.piano_chance.get();
    private static final boolean ShouldPianoExist = Config.SERVER.enable_piano_enchant.get();

    @Override
    public boolean canEnchant(@NotNull ItemStack pStack) {
        return super.canEnchant(pStack) && ShouldPianoExist;
    }
    @Override
    public boolean isTradeable() {
        return ShouldPianoExist;
    }
    @Override
    public boolean isDiscoverable() {
        return ShouldPianoExist;
    }

    @Override
    public @NotNull Component getFullname(int pLevel) {
        MutableComponent mutablecomponent = Component.translatable(this.getDescriptionId());
        if (this.isCurse()) {
            mutablecomponent.withStyle(ChatFormatting.RED);
        } else {
            mutablecomponent.withStyle(ChatFormatting.GRAY);
        }

        if (pLevel != 1 || this.getMaxLevel() != 1) {
            mutablecomponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + pLevel));
        }
        if (!ShouldPianoExist){
            mutablecomponent.append(" [DISABLED]");
        }

        return mutablecomponent;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public void doPostAttack(@NotNull LivingEntity owner, @NotNull Entity target, int level) {
        if (!ShouldPianoExist) return;
        if (owner.getRandom().nextDouble() < BasePianoChance * level){
            GrandPianoProjectile.SpawnPiano(owner, target, 15, GrandPianoProjectile.defaultGravity * level,
                    GrandPianoProjectile.defaultMaxFallSpeed * level,
                    GrandPianoProjectile.defaultDamage + (5 * (level - 1)));
        }
    }
}
