package mod.pilot.jar_of_chaos.mixins.client;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
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
}
