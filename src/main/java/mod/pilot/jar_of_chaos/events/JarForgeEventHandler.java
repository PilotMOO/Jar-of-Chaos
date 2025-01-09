package mod.pilot.jar_of_chaos.events;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;
import mod.pilot.jar_of_chaos.entities.projectiles.JesterArrowProjectile;
import mod.pilot.jar_of_chaos.systems.JarEvents.JarEventHandler;
import mod.pilot.jar_of_chaos.systems.SlimeRain.SlimeRainManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
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
    public static void GetServerFromStarting(ServerStartingEvent event){
        Overworld = event.getServer().overworld();
        JarEventHandler.PopulateEventPool(Overworld);
    }
    @SubscribeEvent
    public static void ServerStart(ServerStartedEvent event){
        ServerLevel server = event.getServer().overworld();
        JarGeneralSaveData.setActiveData(server);
    }

    @SubscribeEvent
    public static void ClearEvents(ServerStoppedEvent event){
        JarEventHandler.PurgeAllEvents();
        System.out.println("Clearing out all Events");
    }

    @SubscribeEvent
    public static void JarEventTicker(TickEvent.ServerTickEvent event){
        JarEventHandler.TickAllEvents();
    }
    @SubscribeEvent
    public static void SlimeRainTicker(TickEvent.ServerTickEvent event){
        if (JarGeneralSaveData.isSlimeRain()) SlimeRainManager.TickSlimeRain();
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
            JarGeneralSaveData.setSlimeRainDuration(arguments.getArgument("Rain Duration", Integer.class));
            ServerLevel server = getServer().getLevel();
            if (!server.isRaining()){
                server.getLevelData().setRaining(true);
            }
            if (arguments.getSource().getEntity() instanceof Player p){
                p.displayClientMessage(Component.literal("Setting slime rain for " + arguments.getArgument("Rain Duration", Integer.class)
                        + " ticks!"), false);
            }
            return 1;
        })));

        event.getDispatcher().register(Commands.literal("clearSlimeRain")
                .then(Commands.argument("Clear normal rain?", BoolArgumentType.bool()).executes(arguments ->{
                    JarGeneralSaveData.setSlimeRainDuration(0);
                    if (arguments.getArgument("Clear normal rain?", Boolean.class)){
                        ServerLevel server = getServer().getLevel();
                        server.getLevelData().setRaining(false);
                    }
                    if (arguments.getSource().getEntity() instanceof Player p){
                        p.displayClientMessage(Component.literal("Clearing slime rain!"), false);
                    }
                    return 1;
                })));

        event.getDispatcher().register(Commands.literal("spawnSlimeKing")
                .then(Commands.argument("Slime Size",IntegerArgumentType.integer()).executes(arguments ->{
                    ServerLevel server = arguments.getSource().getLevel();
                    KingSlimeEntity.SpawnInAt(server, arguments.getSource().getPosition(), arguments.getArgument("Slime Size", Integer.class));
                    if (arguments.getSource().getEntity() instanceof Player p){
                        p.displayClientMessage(Component.literal("Spawned King Slime with size " + arguments.getArgument("Slime Size", Integer.class)
                                + "!"), false);
                    }
                    return 1;
                })));
    }
}
