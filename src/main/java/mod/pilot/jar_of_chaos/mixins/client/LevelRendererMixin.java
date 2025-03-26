package mod.pilot.jar_of_chaos.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import mod.pilot.jar_of_chaos.systems.ModelDisplay.client.BeeModel;
import mod.pilot.jar_of_chaos.systems.ModelDisplay.client.GenericModelHub;
import net.minecraft.client.Camera;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private void InjectModelRendering(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick,
                                      Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci){
        if (jarOfChaos$m == null) {
            jarOfChaos$m = new BeeModel(GenericModelHub.ModelSet.bakeLayer(BeeModel.LAYER_LOCATION));
            System.out.println("Layer location is " + BeeModel.LAYER_LOCATION);
        }
        poseStack.pushPose();
        jarOfChaos$m.renderToBuffer(poseStack, renderBuffers.bufferSource().getBuffer(RenderType.entityTranslucent(BeeModel.TEXTURE_LOCATION)),
                0x00FFFFFF, 0x00FFFFFF, 1f, 1f, 1f, 1f);
        poseStack.popPose();
    }
}
