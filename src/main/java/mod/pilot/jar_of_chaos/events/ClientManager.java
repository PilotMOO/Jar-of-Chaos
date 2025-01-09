package mod.pilot.jar_of_chaos.events;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.entities.client.mobs.ChatteringTeethRenderer;
import mod.pilot.jar_of_chaos.entities.client.mobs.KingSlimeRenderer;
import mod.pilot.jar_of_chaos.entities.client.projectiles.GrandPianoRenderer;
import mod.pilot.jar_of_chaos.entities.client.projectiles.JesterArrowRenderer;
import mod.pilot.jar_of_chaos.particles.JarParticles;
import mod.pilot.jar_of_chaos.particles.StarParticle;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JarOfChaos.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientManager {
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(JarEntities.CHATTERING_TEETH.get(), ChatteringTeethRenderer::new);
        event.registerEntityRenderer(JarEntities.KING_SLIME.get(), KingSlimeRenderer::new);

        event.registerEntityRenderer(JarEntities.PIANO.get(), GrandPianoRenderer::new);
        event.registerEntityRenderer(JarEntities.JESTER_ARROW.get(), JesterArrowRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticle(RegisterParticleProvidersEvent event) {
        Minecraft.getInstance().particleEngine.register(JarParticles.STAR_PARTICLE.get(),
                StarParticle.Provider::new);
    }
}
