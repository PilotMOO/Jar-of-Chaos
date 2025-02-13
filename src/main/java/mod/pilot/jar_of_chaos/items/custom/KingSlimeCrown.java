package mod.pilot.jar_of_chaos.items.custom;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.pilot.jar_of_chaos.items.custom.client.JarRenderer;
import mod.pilot.jar_of_chaos.systems.PlayerSlimeoid.SlimeoidManager;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class KingSlimeCrown extends ArmorItem implements GeoItem {
    private static final ArmorMaterial kSlimeCrownMaterial = new KingSlimeCrownMaterial();
    public KingSlimeCrown(Properties pProperties) {
        super(kSlimeCrownMaterial, Type.HELMET, pProperties.defaultDurability(-1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> swapWithEquipmentSlot(@NotNull Item item, @NotNull Level level,
                                                                             @NotNull Player player, @NotNull InteractionHand hand) {
        InteractionResultHolder<ItemStack> holder = super.swapWithEquipmentSlot(item, level, player, hand);
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(this)){
            SlimeoidManager.addPlayerAsSlimeoid(player);
        } else SlimeoidManager.removePlayerFromSlimeoid(player);
        return holder;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private JarRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new JarRenderer();
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
        AnimationController<GeoItem> animControl = new AnimationController<GeoItem>(this, "CrownAndCapeController",
                event -> PlayState.CONTINUE)
                .triggerableAnim("wave", RawAnimation.begin().thenLoop("wave"));
        animControl.tryTriggerAnimation("wave");
        controllers.add(animControl);
    }

    private final AnimatableInstanceCache cache = createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private static class KingSlimeCrownMaterial implements ArmorMaterial{

        @Override
        public int getDurabilityForType(@NotNull Type pType) {
            return -1;
        }

        @Override
        public int getDefenseForType(@NotNull Type pType) {
            return 4;
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public @NotNull SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_GOLD;
        }

        @Override
        public @NotNull Ingredient getRepairIngredient() {
            return Ingredient.of();
        }

        @Override
        public @NotNull String getName() {
            return "KingSlime";
        }

        @Override
        public float getToughness() {
            return 2;
        }

        @Override
        public float getKnockbackResistance() {
            return -0.25f;
        }
    }
}
