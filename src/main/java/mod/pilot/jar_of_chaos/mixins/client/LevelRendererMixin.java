package mod.pilot.jar_of_chaos.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import mod.pilot.jar_of_chaos.systems.ModelDisplay.client.BeeModel;
import mod.pilot.jar_of_chaos.systems.ModelDisplay.client.GenericModelHub;
import net.minecraft.client.Camera;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow @Final private RenderBuffers renderBuffers;

    @ModifyArg(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private ParticleOptions tickRainSlimeParticleModifier(ParticleOptions pParticleData){
        return JarGeneralSaveData.isSlimeRain() ? ParticleTypes.ITEM_SLIME : pParticleData;
    }

    @Unique
    private static final ResourceLocation SLIME_RAIN = new ResourceLocation(JarOfChaos.MOD_ID, "textures/misc/slime_rain.png");
    @ModifyArg(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    private ResourceLocation renderSlimeRainShaderTexture(ResourceLocation RAIN_TEXTURE){
        return JarGeneralSaveData.isSlimeRain() ? SLIME_RAIN : RAIN_TEXTURE;
    }
    @Unique private Model jarOfChaos$m = null;
    @Inject(method = "renderSky", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFuncSeparate(Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;Lcom/mojang/blaze3d/platform/GlStateManager$SourceFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DestFactor;)V"))
    private void InjectSkyModelRendering(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick,
                                         Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci){
        if (jarOfChaos$m == null) {
            jarOfChaos$m = new BeeModel(GenericModelHub.ModelSet.bakeLayer(BeeModel.LAYER_LOCATION));
        }
        poseStack.pushPose();
        poseStack.translate(0, 300, 0);
        poseStack.scale(100, -100, 100);
        //Stolen from method renderHandsWithItems(args...) in ItemInHandRenderer
        /*if (camera.getEntity() instanceof LocalPlayer p) {
            float xBob = Mth.lerp(partialTick, p.xBobO, p.xBob);
            float yBob = Mth.lerp(partialTick, p.yBobO, p.yBob);
            //Changed from subtraction of f2/xBob and f3/yBob to addition as to cancel out the bob.
            //No clue why this thing was bobbing to begin with smh
            poseStack.mulPose(Axis.XP.rotationDegrees((p.getViewXRot(partialTick) - xBob) * 0.1F));
            poseStack.mulPose(Axis.YP.rotationDegrees((p.getViewYRot(partialTick) - yBob) * 0.1F));
        }*/

        jarOfChaos$m.renderToBuffer(poseStack, renderBuffers.bufferSource().getBuffer(RenderType.entityTranslucent(BeeModel.TEXTURE_LOCATION)),
                15728880, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        poseStack.popPose();
    }
    @Inject(method = "renderLevel", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/RenderBuffers;bufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;"))
    private void RenderBeeInWorld(PoseStack poseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline,
                              Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci){
        if (jarOfChaos$m == null) {
            jarOfChaos$m = new BeeModel(GenericModelHub.ModelSet.bakeLayer(BeeModel.LAYER_LOCATION));
        }
        poseStack.pushPose();
        Vec3 camPos = pCamera.getPosition();
        poseStack.translate(0 - camPos.x, 3 - camPos.y, 0 - camPos.z);
        poseStack.scale(1, -1, 1);
        jarOfChaos$m.renderToBuffer(poseStack, renderBuffers.bufferSource().getBuffer(RenderType.entityTranslucent(BeeModel.TEXTURE_LOCATION)),
                15728880, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        poseStack.popPose();
    }
}
