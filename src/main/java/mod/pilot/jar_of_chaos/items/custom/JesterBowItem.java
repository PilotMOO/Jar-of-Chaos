package mod.pilot.jar_of_chaos.items.custom;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.pilot.jar_of_chaos.entities.projectiles.JesterArrowProjectile;
import mod.pilot.jar_of_chaos.items.custom.client.JesterBowRenderer;
import mod.pilot.jar_of_chaos.systems.JesterArrowEvents.JesterArrowEvent;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class JesterBowItem extends BowItem implements GeoItem {
    public JesterBowItem(Properties pProperties) {
        super(pProperties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    private Class<? extends JesterArrowEvent> lastEventClass;

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private JesterBowRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new JesterBowRenderer();
                }
                return this.renderer;
            }
        });
    }
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "BowManager", event
                -> event.setAndContinue(RawAnimation.begin().thenLoop("Arrowless")))
                .triggerableAnim("Arrowless", RawAnimation.begin().thenLoop("Arrowless"))
                .triggerableAnim("Charge", RawAnimation.begin().then("Charge", Animation.LoopType.PLAY_ONCE)
                        .thenLoop("Hold"))
                .triggerableAnim("Hold", RawAnimation.begin().thenLoop("Hold"))
                .triggerableAnim("Fire", RawAnimation.begin().then("Fire", Animation.LoopType.PLAY_ONCE)
                        .thenLoop("Arrowless")));
    }
    private final AnimatableInstanceCache cache = createInstanceCache(this);
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack bow, @NotNull Level level, @NotNull LivingEntity user, int pTimeLeft) {
        if (user instanceof Player player) {
            boolean flag = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0;
            ItemStack itemstack = player.getProjectile(bow);

            int i = this.getUseDuration(bow) - pTimeLeft;
            i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(bow, level, player, i, !itemstack.isEmpty() || flag);
            if (i < 0) return;

            if (!itemstack.isEmpty() || flag) {
                if (itemstack.isEmpty()) {
                    itemstack = new ItemStack(Items.ARROW);
                }

                if (level instanceof ServerLevel s){
                    triggerAnim(user, GeoItem.getOrAssignId(bow, s), "BowManager", "Fire");
                }

                float f = getPowerForTime(i);
                if (!((double)f < 0.1D)) {
                    boolean flag1 = player.getAbilities().instabuild || (itemstack.getItem() instanceof ArrowItem && ((ArrowItem)itemstack.getItem()).isInfinite(itemstack, bow, player));
                    if (!level.isClientSide) {
                        ArrowItem arrowitem = (ArrowItem)(itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
                        AbstractArrow abstractarrow = arrowitem.createArrow(level, itemstack, player);
                        abstractarrow = customArrow(abstractarrow);
                        abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, 1.0F);

                        if (f == 1.0F) {
                            abstractarrow.setCritArrow(true);
                        }

                        int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
                        if (j > 0) {
                            abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() + (double)j * 0.5D + 0.5D);
                        }

                        int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
                        if (k > 0) {
                            abstractarrow.setKnockback(k);
                        }

                        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) {
                            abstractarrow.setSecondsOnFire(100);
                        }

                        bow.hurtAndBreak(1, player, (p_289501_) -> {
                            p_289501_.broadcastBreakEvent(player.getUsedItemHand());
                        });
                        if (flag1 || player.getAbilities().instabuild && (itemstack.is(Items.SPECTRAL_ARROW) || itemstack.is(Items.TIPPED_ARROW))) {
                            abstractarrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }

                        level.addFreshEntity(abstractarrow);
                    }

                    level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 2F);
                    if (!flag1 && !player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                        if (itemstack.isEmpty()) {
                            player.getInventory().removeItem(itemstack);
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));

                    return;
                }
            }
        }

        if (level instanceof ServerLevel s){
            triggerAnim(user, GeoItem.getOrAssignId(bow, s), "BowManager", "Arrowless");
        }
    }

    @Override
    public void onUseTick(@NotNull Level pLevel, @NotNull LivingEntity LE, @NotNull ItemStack pStack, int pRemainingUseDuration) {
        if (pRemainingUseDuration % 30 == 5){
            pLevel.playSound(null, LE.getX(), LE.getY(), LE.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                    3.0F, (float) (1.5f - LE.getRandom().nextDouble()));
        }
        super.onUseTick(pLevel, LE, pStack, pRemainingUseDuration);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        boolean flag = !player.getProjectile(itemstack).isEmpty();

        InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, level, player, hand, flag);
        if (ret != null) return ret;

        if (!player.getAbilities().instabuild && !flag) {
            if (level instanceof ServerLevel s){
                triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), s), "BowManager", "Arrowless");
            }
            return InteractionResultHolder.fail(itemstack);
        } else {
            player.startUsingItem(hand);
            if (level instanceof ServerLevel s){
                triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), s), "BowManager", "Charge");
            }
            return InteractionResultHolder.consume(itemstack);
        }
    }

    @Override
    public @NotNull AbstractArrow customArrow(@NotNull AbstractArrow arrow) {
        JesterArrowProjectile JArrow = JesterArrowProjectile.CreateWithBlacklist(arrow.level(),
                new ArrayList<>(Collections.singletonList(lastEventClass)));
        JArrow.copyPosition(arrow);
        if (JArrow.Event != null){
            lastEventClass = JArrow.Event.getClass();
        }
        return JArrow;
    }
}
