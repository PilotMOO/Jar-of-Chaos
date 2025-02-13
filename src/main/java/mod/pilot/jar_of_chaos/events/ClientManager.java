package mod.pilot.jar_of_chaos.events;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.entities.client.mobs.ChatteringTeethRenderer;
import mod.pilot.jar_of_chaos.entities.client.mobs.KingSlimeRenderer;
import mod.pilot.jar_of_chaos.entities.client.projectiles.GrandPianoRenderer;
import mod.pilot.jar_of_chaos.entities.client.projectiles.JesterArrowRenderer;
import mod.pilot.jar_of_chaos.entities.client.projectiles.SlimeArrowRenderer;
import mod.pilot.jar_of_chaos.particles.JarParticles;
import mod.pilot.jar_of_chaos.particles.StarParticle;
import mod.pilot.jar_of_chaos.systems.PlayerSlimeoid.client.AbstractSlimeoidModel;
import mod.pilot.jar_of_chaos.systems.PlayerSlimeoid.client.SlimSlimeoidModel;
import mod.pilot.jar_of_chaos.systems.PlayerSlimeoid.client.SlimeoidLayer;
import mod.pilot.jar_of_chaos.systems.PlayerSlimeoid.client.SlimeoidModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
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
        event.registerEntityRenderer(JarEntities.SLIME_BALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(JarEntities.SLIME_ARROW.get(), SlimeArrowRenderer::new);
    }
    @SubscribeEvent
    public static void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(SlimeoidModel.LAYER_LOCATION, SlimeoidModel::createBodyLayer);
        event.registerLayerDefinition(SlimSlimeoidModel.LAYER_LOCATION, SlimSlimeoidModel::createBodyLayer);
    }
    @SubscribeEvent
    public static void addLayers(final EntityRenderersEvent.AddLayers event){
        //Fuck it, mixin time
        /*System.out.println("Attempting to apply SlimeoidLayer to player model...");
        LivingEntityRenderer<Player, HumanoidModel<Player>> renderer = event.getRenderer(EntityType.PLAYER);
        if (renderer != null){
            System.out.println("SlimeoidLayer has been applied to the player model");
            boolean flag = renderer.addLayer(new SlimeoidLayer<>(renderer, false));
            System.out.println("addLayer returned " + flag + "!");
        } else System.err.println("Oops! event.getRenderer(EntityType.PLAYER) returned null! Bowomp.");

        System.out.println("Attempting to apply SlimeoidLayer to zombie model...");
        LivingEntityRenderer<Zombie, HumanoidModel<Zombie>> zRenderer = event.getRenderer(EntityType.ZOMBIE);
        if (zRenderer != null){
            System.out.println("SlimeoidLayer has been applied to the zombie model!");
            boolean flag = zRenderer.addLayer(new SlimeoidLayer<>(zRenderer, false));
            System.out.println("addLayer returned " + flag + "!");
        } else System.err.println("Oops! event.getRenderer(EntityType.ZOMBIE) returned null! Bowomp.");*/
    }

    @SubscribeEvent
    public static void registerParticle(RegisterParticleProvidersEvent event) {
        Minecraft.getInstance().particleEngine.register(JarParticles.STAR_PARTICLE.get(),
                StarParticle.Provider::new);
    }
}
