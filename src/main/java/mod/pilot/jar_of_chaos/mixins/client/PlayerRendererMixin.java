package mod.pilot.jar_of_chaos.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.pilot.jar_of_chaos.systems.PlayerGeloid.GeloidManager;
import mod.pilot.jar_of_chaos.systems.PlayerGeloid.client.SlimGeloidModel;
import mod.pilot.jar_of_chaos.systems.PlayerGeloid.client.GeloidLayer;
import mod.pilot.jar_of_chaos.systems.PlayerGeloid.client.GeloidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerRendererMixin(EntityRendererProvider.Context pContext, PlayerModel<AbstractClientPlayer> pModel, float pShadowRadius) {
        super(pContext, pModel, pShadowRadius);
    }
    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
    at = @At(value = "HEAD"))
    private void InjectSquish(AbstractClientPlayer pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack,
                              MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci){
        GeloidManager.GeloidPacket packet = GeloidManager.getPacketFor(pEntity);
        if (packet != null){
            float vLerp = packet.LerpSquish(true);
            float hLerp = 1 + ((1 - vLerp));
            pPoseStack.scale(hLerp, vLerp, hLerp);
        }
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void InjectGeloidLayer(EntityRendererProvider.Context pContext, boolean pUseSlimModel, CallbackInfo ci){
        this.addLayer(new GeloidLayer<>(this,
                pContext.bakeLayer(pUseSlimModel ? SlimGeloidModel.LAYER_LOCATION : GeloidModel.LAYER_LOCATION),
                pUseSlimModel));
    }
}
