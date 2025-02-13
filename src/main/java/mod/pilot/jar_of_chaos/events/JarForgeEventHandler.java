package mod.pilot.jar_of_chaos.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mod.pilot.jar_of_chaos.Config;
import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import mod.pilot.jar_of_chaos.effects.JarEffects;
import mod.pilot.jar_of_chaos.effects.SplatEffect;
import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;
import mod.pilot.jar_of_chaos.entities.projectiles.JesterArrowProjectile;
import mod.pilot.jar_of_chaos.sound.JarSounds;
import mod.pilot.jar_of_chaos.systems.ChatteringTeethSoundManager;
import mod.pilot.jar_of_chaos.systems.JarEvents.JarEventHandler;
import mod.pilot.jar_of_chaos.systems.PlayerGeloid.GeloidManager;
import mod.pilot.jar_of_chaos.systems.SlimeRain.KingSlimeBossEventManager;
import mod.pilot.jar_of_chaos.systems.SlimeRain.SlimeRainManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

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
        SplatEffect.Flush();
        System.out.println("[SPLAT EFFECT HANDLER] Flushing all tracked velocities");
        ChatteringTeethSoundManager.Flush();
        System.out.println("[CHATTERING TEETH SFX HANDLER] Flushing all tracked sound positions");
    }

    private static final boolean kirby = Config.SERVER.should_kirby.get();
    private static final int kirbyCrashThreshold = 30;
    private static final HashMap<ServerPlayer, Integer> playerKirbyMap = kirby ? new HashMap<>() : null;
    @SubscribeEvent
    public static void KirbyServerTick(TickEvent.ServerTickEvent event) throws Exception {
        if (!kirby) return;
        for (ServerPlayer sp : playerKirbyMap.keySet()){
            playerKirbyMap.replace(sp, playerKirbyMap.get(sp) + 1);
            if (playerKirbyMap.get(sp) % 20 == 0){
                sp.level().playSound(null, BlockPos.containing(sp.position()),
                        JarSounds.KIRBY.get(), SoundSource.VOICE, 1f, 1f);
            }
            if (playerKirbyMap.get(sp) >= kirbyCrashThreshold + sp.latency){
                sp.connection.disconnect(Component.translatable("jar_of_chaos.kirby_disconnect"));
                //throw new Exception("Kirby has entered the config");
            }
        }
    }
    @SubscribeEvent
    public static void KirbyPlayerJoin(EntityJoinLevelEvent event){
        if (kirby && event.getEntity() instanceof ServerPlayer sp){
            playerKirbyMap.put(sp, 0);
        }
    }

    @SubscribeEvent
    public static void JarServerTicker(TickEvent.ServerTickEvent event){
        JarEventHandler.TickAllEvents();
        if (JarGeneralSaveData.isSlimeRain()) SlimeRainManager.TickSlimeRain(event.getServer().overworld());
        ChatteringTeethSoundManager.tick();
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
    public static void GeloidSlimeTargetHook(LivingChangeTargetEvent event){
        if (event.getEntity() instanceof Slime && GeloidManager.isActiveGeloid(event.getNewTarget())){
            event.setCanceled(true);
        }
    }


    //Player Bounce Handler (special thanks to the devs of TConstruct for having a public GitHub <3)

    private static final ArrayList<BounceInstance> bouncingArrayList = new ArrayList<>();
    public static ArrayList<BounceInstance> getBounceInstances(){
        return new ArrayList<>(bouncingArrayList);
    }
    public static void AddToBounceMap(Player player){
        bouncingArrayList.add(new BounceInstance(player));
    }
    public static void RemoveFromBounceMap(Player player){
        for (BounceInstance reader : getBounceInstances()){
            if (reader.player == player){
                bouncingArrayList.remove(reader);
                return;
            }
        }
    }
    public static @Nullable BounceInstance retrieveBounceInstanceFor(Player player){
        for (BounceInstance reader : getBounceInstances()){
            if (reader.player == player){
                return reader;
            }
        }
        return null;
    }
    @SubscribeEvent
    public static void ReadAndWritePlayerBounce(LivingFallEvent event){
        if (event.getDistance() > 1.5 && event.getEntity() instanceof Player player && isBouncy(player)){
            AddToBounceMap(player);
            event.setDamageMultiplier(0);
        }
    }
    @SubscribeEvent
    public static void ApplyPlayerBounce(LivingEvent.LivingTickEvent event){
        BounceInstance bounce;
        if (event.getEntity() instanceof Player player && isBouncy(player)
            && (bounce = retrieveBounceInstanceFor(player)) != null){
            Vec3 delta = bounce.oldDelta.multiply(0.95, -0.95, 0.95);
            if (player.isSecondaryUseActive()) delta = delta.multiply(0.1, 0.1, 0.1);
            player.setDeltaMovement(delta);
            player.playSound(JarSounds.BOING.get());
            RemoveFromBounceMap(player);
        }
    }
    private static boolean isBouncy(Player player){
        return player.hasEffect(JarEffects.SPLAT.get()) || GeloidManager.isActiveGeloid(player);
    }
    private record BounceInstance(Player player, Vec3 oldDelta) {
        public BounceInstance(@NotNull Player player, @NotNull Vec3 oldDelta){
            this.player = player;
            this.oldDelta = oldDelta;
        }
        public BounceInstance(@NotNull Player target){
            this(target, target.getDeltaMovement());
        }
    }


    @SubscribeEvent
    public static void RegisterCommands(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("setSlimeRain")
                .then(Commands.argument("Rain Duration",IntegerArgumentType.integer()).executes(arguments ->{
                    SlimeRainManager.StartSlimeRain(getServer().getLevel(), arguments.getArgument("Rain Duration", Integer.class), true);
                    if (arguments.getSource().getEntity() instanceof Player p){
                        p.displayClientMessage(Component.literal("Starting Slime Rain for "
                                + arguments.getArgument("Rain Duration", Integer.class) + " ticks!"), false);
                    }
            return 1;
        })));

        event.getDispatcher().register(Commands.literal("clearSlimeRain")
                .then(Commands.argument("Clear normal rain?", BoolArgumentType.bool()).executes(arguments ->{
                    ServerLevel server = getServer().getLevel();
                    SlimeRainManager.StopSlimeRain(server, arguments.getArgument("Clear normal rain?", Boolean.class), true, false);
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

        event.getDispatcher().register(Commands.literal("setClearWeatherTime")
                .then(Commands.argument("time",IntegerArgumentType.integer()).executes(arguments ->{
                    clearWeatherTime = arguments.getArgument("time", Integer.class);
                    return 1;
                })));
    }
    public static int clearWeatherTime;

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
