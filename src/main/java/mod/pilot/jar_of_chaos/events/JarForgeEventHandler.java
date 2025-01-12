package mod.pilot.jar_of_chaos.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;
import mod.pilot.jar_of_chaos.entities.projectiles.JesterArrowProjectile;
import mod.pilot.jar_of_chaos.systems.JarEvents.JarEventHandler;
import mod.pilot.jar_of_chaos.systems.SlimeRain.KingSlimeBossEventManager;
import mod.pilot.jar_of_chaos.systems.SlimeRain.SlimeRainManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = JarOfChaos.MOD_ID)
public class JarForgeEventHandler {
    private static ServerLevel Overworld;
    public static @NotNull ServerLevel getServer(){
        return Overworld;
    }

    @SubscribeEvent
    public static void getServerFromStarting(ServerStartingEvent event){
        Overworld = event.getServer().overworld();
        JarEventHandler.PopulateEventPool(Overworld);
    }
    @SubscribeEvent
    public static void ServerStart(ServerStartedEvent event){
        ServerLevel server = event.getServer().overworld();
        JarGeneralSaveData.setActiveData(server);
    }

    @SubscribeEvent
    public static void ServerStop(ServerStoppedEvent event){
        JarEventHandler.PurgeAllEvents();
        System.out.println("[JAR EVENT HANDLER] Clearing out all Events");
        SlimeRainManager.FlushPOIs();
        System.out.println("[SLIME RAIN EVENT HANDLER] Flushing POIs");
        SlimeRainManager.resetAllTrackers();
        System.out.println("[SLIME RAIN EVENT HANDLER] Resetting all Trackers");
    }

    @SubscribeEvent
    public static void JarEventsTicker(TickEvent.ServerTickEvent event){
        JarEventHandler.TickAllEvents();
        if (JarGeneralSaveData.isSlimeRain()) SlimeRainManager.TickSlimeRain(event.getServer().overworld());
    }
    @SubscribeEvent
    public static void SlimeDeathTracker(EntityLeaveLevelEvent event){
        if (!JarGeneralSaveData.isSlimeRain() || !(event.getLevel() instanceof ServerLevel)) return;
        Entity entity = event.getEntity();
        if (event.getLevel().getEntitiesOfClass(KingSlimeEntity.class, entity.getBoundingBox().inflate(SlimeRainManager.POIRange)).size() > 0) {
            System.out.println("Nearby slime king spotted, no longer awarding kills...");
            return;
        }
        if (entity instanceof Slime slime && slime.isDeadOrDying()){
            SlimeRainManager.AwardKillToNearestPOI(slime.position());
            System.out.println("Awarded a kill to the nearest POI!");
        }
    }

    @SubscribeEvent
    public static void JesterArrowEventTracker(EntityJoinLevelEvent event){
        if (event.getLevel().getServer() == null || !event.getLevel().getServer().isReady()) return;
        Entity E = event.getEntity();
        if (E instanceof JesterArrowProjectile J && J.Event != null && !J.getEventFired()){
            J.Event.OnSpawn();
        }
    }

    @SubscribeEvent
    public static void KingSlimeCameraDismountFixer(EntityMountEvent event){
        if (event.isDismounting()){
            Entity passenger = event.getEntityMounting();
            Entity mounted = event.getEntityBeingMounted();

            if (passenger == null || mounted == null) return;
            if (passenger instanceof Player p && mounted instanceof KingSlimeEntity kSlime){
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player.getGameProfile() == p.getGameProfile()){
                    int cameraType = kSlime.getOldCameraType();
                    if (cameraType == -1){
                        System.err.println("Something went wrong! KingSlimeEntity.getOldCameraType() returned -1!");
                        return;
                    }
                    CameraType oldCamera = CameraType.values()[kSlime.getOldCameraType()];
                    minecraft.options.setCameraType(oldCamera);
                    kSlime.setOldCameraType(-1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void RegisterCommands(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("setSlimeRain")
                .then(Commands.argument("Rain Duration",IntegerArgumentType.integer()).executes(arguments ->{
                    SlimeRainManager.StartSlimeRain(getServer().getLevel(), arguments.getArgument("Rain Duration", Integer.class));
                    if (arguments.getSource().getEntity() instanceof Player p){
                        p.displayClientMessage(Component.literal("Starting Slime Rain for "
                                + arguments.getArgument("Rain Duration", Integer.class) + " ticks!"), false);
                    }
            return 1;
        })));

        event.getDispatcher().register(Commands.literal("clearSlimeRain")
                .then(Commands.argument("Clear normal rain?", BoolArgumentType.bool()).executes(arguments ->{
                    ServerLevel server = getServer().getLevel();
                    SlimeRainManager.StopSlimeRain(server, arguments.getArgument("Clear normal rain?", Boolean.class));
                    if (arguments.getSource().getEntity() instanceof Player p){
                        p.displayClientMessage(Component.literal("Clearing slime rain!"), false);
                    }
                    return 1;
                })));

        event.getDispatcher().register(Commands.literal("spawnSlimeKing")
                .then(Commands.argument("Slime Size",IntegerArgumentType.integer()).executes(arguments ->{
                    ServerLevel server = arguments.getSource().getLevel();
                    KingSlimeEntity.SpawnInAt(server, arguments.getSource().getPosition(), arguments.getArgument("Slime Size", Integer.class), false);
                    if (arguments.getSource().getEntity() instanceof Player p){
                        p.displayClientMessage(Component.literal("Spawned King Slime with size " + arguments.getArgument("Slime Size", Integer.class)
                                + "!"), false);
                    }
                    return 1;
                })));
    }

    private static final ResourceLocation SLIME_KING_BOSSBAR = new ResourceLocation(JarOfChaos.MOD_ID, "textures/misc/king_slime_bossbar.png");
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderBossOverlay(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (KingSlimeBossEventManager.checkBossEventsFor(event.getBossEvent().getId())) {

            int i = event.getGuiGraphics().guiWidth();
            int j = event.getY();
            Component component = event.getBossEvent().getName();
            event.setCanceled(true);
            event.getGuiGraphics().blit(SLIME_KING_BOSSBAR, event.getX(), event.getY(), 0, 0, 182, 15);
            int progressScaled = (int) (event.getBossEvent().getProgress() * 183.0F);
            event.getGuiGraphics().blit(SLIME_KING_BOSSBAR, event.getX(), event.getY(), 0, 15, progressScaled, 15);
            int l = Minecraft.getInstance().font.width(component) + (i / 9);
            int textX = i / 2 - l / 2;
            int textY = j - 5;
            PoseStack poseStack = event.getGuiGraphics().pose();
            poseStack.pushPose();
            poseStack.translate(textX, textY, 0);
            Minecraft.getInstance().font.drawInBatch8xOutline(component.getVisualOrderText(), 0.0F, 0.0F, 9230210, 3429167, poseStack.last().pose(), event.getGuiGraphics().bufferSource(), 240);
            poseStack.popPose();
            event.setIncrement(event.getIncrement() + 7);
        }
    }
}
