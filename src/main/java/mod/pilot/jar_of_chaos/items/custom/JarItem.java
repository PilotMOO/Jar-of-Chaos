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
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
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

    /*
    @Override
    public void inventoryTick(@NotNull ItemStack itemStack, @NotNull Level level, @NotNull Entity entity, int pSlotId, boolean pIsSelected) {
        if (entity instanceof Player player && level instanceof ServerLevel server){
            triggerAnim(player, GeoItem.getOrAssignId(itemStack, server), "JarAnimationController", "Stable");
        }
    }*/

    /*
    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        System.out.println("Trying to set up animations for the jar");
        triggerAnim(GeoItem.getOrAssignId(new ItemStack(this), JarForgeEventHandler.getServer()), "JarAnimationController", "Stable", new AzureLibNetwork.IPacketCallback() {
            @Override
            public void onReadyToSend(AbstractPacket packetToSend) {
                return;
            }
        });
    }*/
}
