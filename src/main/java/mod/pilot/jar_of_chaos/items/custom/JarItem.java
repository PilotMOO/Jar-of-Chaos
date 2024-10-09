package mod.pilot.jar_of_chaos.items.custom;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.pilot.jar_of_chaos.events.JarForgeEventHandler;
import mod.pilot.jar_of_chaos.items.custom.client.JarRenderer;
import mod.pilot.jar_of_chaos.systems.JarEvents.JarEvent;
import mod.pilot.jar_of_chaos.systems.JarEvents.JarEventHandler;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class JarItem extends Item implements GeoItem {
    public JarItem(Properties pProperties) {
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
            JarEvent newEvent = JarEventHandler.getRandomCloneFromEventPool(server, player, player.position());
            int cycleTracker = 0;
            while ((newEvent == null || (LastEvent != null && newEvent.getClass() == getLastEvent().getClass())) && cycleTracker < 5){
                newEvent = JarEventHandler.getRandomCloneFromEventPool(server, player, player.position());
                cycleTracker++;
            }
            JarEvent.Subscribe(newEvent);
            player.getCooldowns().addCooldown(this, 60);
            player.playSound(SoundEvents.GLASS_BREAK);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, new ItemStack(this));
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
