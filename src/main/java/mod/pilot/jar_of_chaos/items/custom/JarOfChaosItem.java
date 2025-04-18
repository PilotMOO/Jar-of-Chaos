package mod.pilot.jar_of_chaos.items.custom;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.pilot.jar_of_chaos.items.custom.client.JarOfChaosRenderer;
import mod.pilot.jar_of_chaos.systems.JarEvents.JarEvent;
import mod.pilot.jar_of_chaos.systems.JarEvents.JarEventHandler;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class JarOfChaosItem extends Item implements GeoItem {
    public JarOfChaosItem(Properties pProperties) {
        super(pProperties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    private JarEvent LastEvent;
    public JarEvent getLastEvent(){
        return LastEvent;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level instanceof ServerLevel server){
            JarEvent newEvent;
            int cycleTracker = 0;
            do{
                newEvent = JarEventHandler.getRandomCloneFromEventPool(server, player, player.position());
                cycleTracker++;
            }
            while ((newEvent == null || (LastEvent != null && newEvent.getClass() == getLastEvent().getClass())) && cycleTracker < 5);
            if (newEvent != null){
                newEvent.Subscribe();
            }
            player.getCooldowns().addCooldown(this, 60);
            player.playSound(SoundEvents.GLASS_BREAK);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, new ItemStack(this));
    }



    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private JarOfChaosRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new JarOfChaosRenderer();
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
        AnimationController<GeoItem> animControl = new AnimationController<GeoItem>(this, "JarAnimationController",
                event -> PlayState.CONTINUE)
                .triggerableAnim("Stable", RawAnimation.begin().thenLoop("Stable"));
        animControl.tryTriggerAnimation("Stable");
        controllers.add(animControl);
    }

    private final AnimatableInstanceCache cache = createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
