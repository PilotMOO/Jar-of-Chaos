package mod.pilot.jar_of_chaos.items.custom;

import mod.pilot.jar_of_chaos.entities.projectiles.SlimeArrowProjectile;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SlimeArrowItem extends ArrowItem {
    public SlimeArrowItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull AbstractArrow createArrow(@NotNull Level level, @NotNull ItemStack stack, @NotNull LivingEntity shooter) {
        return SlimeArrowProjectile.create(shooter, level);
    }
}
