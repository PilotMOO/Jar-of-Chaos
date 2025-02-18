package mod.pilot.jar_of_chaos.systems.SlimeRain;

import mod.pilot.jar_of_chaos.Config;
import mod.pilot.jar_of_chaos.blocks.JarBlocks;
import mod.pilot.jar_of_chaos.data.IntegerCycleTracker;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;
import mod.pilot.jar_of_chaos.entities.projectiles.SlimeBallProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;

public class SlimeRainManager {
    public static void Setup(){
        MinecraftForge.EVENT_BUS.addListener(SlimeRainManager::TickSlimeRain);
        MinecraftForge.EVENT_BUS.addListener(SlimeRainManager::SlimeDeathTracker);
        MinecraftForge.EVENT_BUS.addListener(SlimeRainManager::ServerClosingCleanUp);
    }

    private static final Random random = new Random();

    public static final int DefaultPOIRange = Config.SERVER.slime_rain_poi_range.get();
    public static int POIRange = DefaultPOIRange;
    public static final int DefaultNeededKills = Config.SERVER.slime_rain_poi_kills.get();
    public static int NeededKills = DefaultNeededKills;
    private static ArrayList<SlimeRainPOI> POIs;

    private static final int kingSlimeStartingSize = Config.SERVER.king_slime_starting_size.get();

    public static @Nullable ArrayList<SlimeRainPOI> getPOIs(){
        return POIs == null ? null : new ArrayList<>(POIs);
    }
    public static void SetupPOIList(){
        POIs = new ArrayList<>();
    }
    public static void AddToPOIs(SlimeRainPOI toAdd){
        POIs.add(toAdd);
        JarGeneralSaveData.Dirty();
    }
    public static void quietAddToPOIs(SlimeRainPOI toAdd){
        POIs.add(toAdd);
    }
    public static void FlushPOIs(){
        if (POIs != null){
            POIs.clear();
            JarGeneralSaveData.Dirty();
        }
        System.out.println("[SLIME RAIN EVENT HANDLER] Flushing POIs");
    }

    public static boolean finalizing = false;
    public static boolean fromBoss = false;

    private static final IntegerCycleTracker.Randomized SlimeSpawnTracker = new IntegerCycleTracker.Randomized(300, 200);
    private static final IntegerCycleTracker.Randomized SlimeBallProjTracker = new IntegerCycleTracker.Randomized(120, 80);
    private static final IntegerCycleTracker.Randomized SlimeLayerTracker = new IntegerCycleTracker.Randomized(60, 40);
    private static final IntegerCycleTracker KingSlimeKillCountTracker = new IntegerCycleTracker(100);
    private static final IntegerCycleTracker BossBarUpdateTracker = new IntegerCycleTracker(60);
    private static final IntegerCycleTracker RefreshRainTracker = new IntegerCycleTracker(100);
    public static void ResetAllTrackers(){
        SlimeSpawnTracker.reset();
        SlimeBallProjTracker.reset();
        SlimeLayerTracker.reset();
        KingSlimeKillCountTracker.reset();
        BossBarUpdateTracker.reset();
        RefreshRainTracker.reset();
        System.out.println("[SLIME RAIN EVENT HANDLER] Resetting all trackers");
    }


    public static void StartSlimeRain(ServerLevel server, int rainFor, boolean quiet){
        StartSlimeRain(server, rainFor, DefaultPOIRange, DefaultNeededKills, quiet);
    }
    public static void StartSlimeRain(ServerLevel server, int rainFor, int POIRangeOverride, int neededKillsOverride, boolean quiet){
        JarGeneralSaveData.setSlimeRainDuration(rainFor);
        server.getLevelData().setRaining(true);

        POIRange = POIRangeOverride;
        NeededKills = neededKillsOverride;
        POIs = new ArrayList<>();
        finalizing = false;

        if (!quiet){
            for (ServerPlayer player : server.getPlayers((s) -> true)){
                player.sendSystemMessage(Component.translatable("jar_of_chaos.slime_rain.start"));
            }
        }
    }
    public static void StopSlimeRain(@Nullable ServerLevel server, boolean force, boolean quiet, boolean fromBoss){
        if (force) finalizeSlimeRain(server, true, quiet, fromBoss);
        else internalGradualStopSlimeRain(server, fromBoss);
    }
    private static void internalGradualStopSlimeRain(@Nullable ServerLevel server, boolean fromBoss){
        JarGeneralSaveData.setSlimeRainDuration(180);
        if (server != null) server.getLevelData().setRaining(false);
        SlimeRainManager.fromBoss = fromBoss;
        finalizing = true;
    }
    private static void finalizeSlimeRain(){
        finalizeSlimeRain(null, false, true, false);
    }
    private static void finalizeSlimeRain(@Nullable ServerLevel server, boolean stopNormalRain, boolean quiet, boolean fromBoss){
        for (SlimeRainPOI poi : POIs) poi.terminate();
        POIs = null;
        playerPOIMap.clear();
        POIRange = DefaultPOIRange;
        NeededKills = DefaultNeededKills;
        finalizing = false;
        ResetAllTrackers();

        JarGeneralSaveData.setSlimeRainDuration(0);
        if (stopNormalRain && server != null) server.getLevelData().setRaining(false);
        JarGeneralSaveData.Dirty();

        if (!quiet && server != null){
            for (ServerPlayer player : server.getPlayers((s) -> true)){
                player.sendSystemMessage(Component.translatable(
                        fromBoss ? "jar_of_chaos.slime_rain.end_bosskill" : "jar_of_chaos.slime_rain.end_generic"));
            }
        }
    }


    public static void TickSlimeRain(TickEvent.ServerTickEvent event){
        ServerLevel server = event.getServer().overworld();
        if (!JarGeneralSaveData.isSlimeRain()) return;
        JarGeneralSaveData.tickSlimeRainDuration();

        if (!JarGeneralSaveData.isSlimeRain()){
            StopSlimeRain(server, finalizing, false, fromBoss);
        }
        if (finalizing) return;

        if (SlimeSpawnTracker.tick()){
            final int hOffset = 32;
            final int vOffsetLower = 16;
            final int vOffsetUpper = 64;
            for (ServerPlayer player : server.getPlayers((s) -> true)){
                int slimeCount = random.nextInt(1, 5);
                for (int i = 0; i < slimeCount; i++){
                    Vec3 spawnPos = findSurfaceThenAdd(server,
                            player.position().add(random.nextInt(-hOffset, hOffset), 0, random.nextInt(-hOffset, hOffset)),
                            random.nextInt(vOffsetLower, vOffsetUpper));
                    generateSlimeEntityAt(server, spawnPos);
                }
            }
        }
        if (SlimeBallProjTracker.tick()){
            final int hOffset = 12;
            final int vOffsetLower = 16;
            final int vOffsetUpper = 32;
            for (ServerPlayer player : server.getPlayers((s) -> true)){
                int projCount = random.nextInt(3,9);
                for (int i = 0; i < projCount; i++){
                    Vec3 spawnPos = findSurfaceThenAdd(server,
                            player.position().add(random.nextInt(-hOffset, hOffset), 0, random.nextInt(-hOffset, hOffset)),
                            random.nextInt(vOffsetLower, vOffsetUpper));
                    SlimeBallProjectile.createAt(server, spawnPos, new Vec3(0,
                            -1 * random.nextDouble(), 0), random.nextInt(5));
                }
            }
        }
        if (SlimeLayerTracker.tick()){
            int layerCount = random.nextInt(5, 10);
            for (int i = 0; i < layerCount; i++){
                int x = random.nextInt(-48, 48);
                int z = random.nextInt(-48, 48);
                for (ServerPlayer player : server.getPlayers((s) -> true)){
                    Vec3 pos = player.position().add(x, 0, z);
                    BlockPos slimePos = findSlimeLayerPosition(pos, server);
                    BlockState bState = server.getBlockState(slimePos);
                    if (bState.is(JarBlocks.SLIME_LAYER.get()) && bState.getValue(SnowLayerBlock.LAYERS) < 8){
                        server.setBlock(slimePos, bState.setValue(SnowLayerBlock.LAYERS, bState.getValue(SnowLayerBlock.LAYERS) + 1), 3);
                    } else server.setBlock(slimePos, JarBlocks.SLIME_LAYER.get().defaultBlockState(), 3);
                }
            }
        }
        if (KingSlimeKillCountTracker.tick()){
            ArrayList<SlimeRainPOI> validPOIs = CheckAndGatherPOIsWithKillCount(NeededKills);
            if (validPOIs != null){
                for (SlimeRainPOI poi : validPOIs){
                    Vec3 pos = findSurfaceThenAdd(server, poi.position, 50);
                    KingSlimeEntity.SpawnInAt(server, pos, kingSlimeStartingSize, true);
                    poi.terminate();
                    POIs.remove(poi);

                    for (ServerPlayer player : server.getPlayers((s) -> true)){
                        player.sendSystemMessage(Component.translatable("jar_of_chaos.slime_rain.boss_spawn"));
                        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                                SoundSource.HOSTILE, 1.0f, 0.75f);
                    }
                }
            }
        }
        if (BossBarUpdateTracker.tick()){
            for (ServerPlayer player : server.getPlayers((s) -> true)){
                SlimeRainPOI closest = getClosestPOIWithinDistance(player.position());
                if (closest != null){
                    if (playerPOIMap.containsKey(player)) playerPOIMap.replace(player, closest);
                    else playerPOIMap.put(player, closest);
                }
            }
        }
        if (RefreshRainTracker.tick()) server.getLevelData().setRaining(true);
    }
    public static void ServerClosingCleanUp(ServerStoppedEvent event){
        FlushPOIs();
        ResetAllTrackers();
        KingSlimeBossEventManager.Clear();
    }
    public static void SlimeDeathTracker(EntityLeaveLevelEvent event){
        if (!JarGeneralSaveData.isSlimeRain() || !(event.getLevel() instanceof ServerLevel)) return;
        Entity entity = event.getEntity();
        if (event.getLevel().getEntitiesOfClass(KingSlimeEntity.class, entity.getBoundingBox().inflate(SlimeRainManager.POIRange)).size() > 0) {
            return;
        }
        if (entity instanceof Slime slime && slime.isDeadOrDying()){
            SlimeRainManager.AwardKillToNearestPOI(slime.position());
        }
    }


    private static BlockPos findSlimeLayerPosition(Vec3 start, Level level){
        BlockPos.MutableBlockPos mBPos = new BlockPos.MutableBlockPos(start.x, start.y, start.z);
        BlockState bState = level.getBlockState(mBPos);
        while (!level.canSeeSky(mBPos)){
            mBPos.move(0, 1, 0);
            bState = level.getBlockState(mBPos);
        }
        BlockState belowState = level.getBlockState(mBPos.below());
        while (belowState.canBeReplaced() || belowState.is(JarBlocks.SLIME_LAYER.get()) && belowState.getValue(SnowLayerBlock.LAYERS) < 8){
            if (bState.is(JarBlocks.SLIME_LAYER.get()) && bState.getValue(SnowLayerBlock.LAYERS) < 8){
                return mBPos;
            }
            mBPos.move(0, -1, 0);
            bState = level.getBlockState(mBPos);
            belowState = level.getBlockState(mBPos.below());
        }
        return mBPos;
    }
    private static Vec3 findSurfaceThenAdd(ServerLevel server, Vec3 position, int addedY) {
        BlockPos.MutableBlockPos mBPos = BlockPos.containing(position).mutable();
        BlockState bState;
        do{
            mBPos.move(0, 1, 0);
            bState = server.getBlockState(mBPos);
        }
        while (!bState.isAir() && !server.canSeeSky(mBPos));
        return mBPos.getCenter().add(0, addedY, 0);
    }
    private static final int minSize = 1;
    private static final int maxSize = 6;
    private static void generateSlimeEntityAt(ServerLevel server, Vec3 spawnPos) {
        Slime slime = new Slime(EntityType.SLIME, server){
            @Override
            public boolean causeFallDamage(float pFallDistance, float pMultiplier, @NotNull DamageSource pSource) {
                return false;
            }
        };
        slime.setSize(random.nextInt(minSize, maxSize), true);
        slime.setPos(spawnPos);
        server.addFreshEntity(slime);
    }



    public static SlimeRainPOI getClosestPOI(Vec3 pos){
        SlimeRainPOI closest = null;
        double dist = Double.MAX_VALUE;
        for (SlimeRainPOI poi : POIs){
            double dist1 = poi.distance(pos);
            if (dist1 < dist){
                closest = poi;
                dist = dist1;
            }
        }

        return closest;
    }
    public static @Nullable SlimeRainPOI getClosestPOIWithinDistance(Vec3 pos){
        SlimeRainPOI closest = null;
        double dist = Double.MAX_VALUE;
        for (SlimeRainPOI poi : POIs){
            double dist1 = poi.distanceIfInRange(pos);
            if (dist1 == -1) continue;
            if (dist1 < dist){
                closest = poi;
                dist = dist1;
            }
        }
        return closest;
    }
    public static SlimeRainPOI getOrCreatePOINear(Vec3 pos){
        return Objects.requireNonNullElseGet(getClosestPOIWithinDistance(pos), () -> new SlimeRainPOI(pos));
    }

    public static void AwardKillToNearestPOI(Vec3 pos){
        AwardKillToNearestPOI(pos, 1);
    }
    public static void AwardKillToNearestPOI(Vec3 pos, int count){
        getOrCreatePOINear(pos).addKills(count);
    }

    public static @Nullable ArrayList<SlimeRainPOI> CheckAndGatherPOIsWithKillCount(int killCount){
        ArrayList<SlimeRainPOI> valid = null;
        for (SlimeRainPOI poi : POIs){
            if (poi.getKills() >= killCount){
                if (valid == null) valid = new ArrayList<>();
                valid.add(poi);
            }
        }
        return valid;
    }

    private static final HashMap<ServerPlayer, SlimeRainPOI> playerPOIMap = new HashMap<>(){
        @Override
        public SlimeRainPOI put(ServerPlayer key, SlimeRainPOI value) {
            value.addPlayerToBossEvent(key);
            return super.put(key, value);
        }

        @Override
        public boolean replace(ServerPlayer key, SlimeRainPOI oldValue, SlimeRainPOI newValue) {
            oldValue.removePlayerFromBossEvent(key);
            newValue.addPlayerToBossEvent(key);
            return super.replace(key, oldValue, newValue);
        }

        @Override
        public SlimeRainPOI replace(ServerPlayer key, SlimeRainPOI value) {
            this.get(key).removePlayerFromBossEvent(key);
            value.addPlayerToBossEvent(key);
            return super.replace(key, value);
        }

        @Override
        public SlimeRainPOI remove(Object key) {
            if (key instanceof ServerPlayer s){
                this.get(s).removePlayerFromBossEvent(s);
            }
            return super.remove(key);
        }

        @Override
        public boolean remove(Object key, Object value) {
            if (key instanceof ServerPlayer player && value instanceof SlimeRainPOI poi){
                poi.removePlayerFromBossEvent(player);
            }
            return super.remove(key, value);
        }

        @Override
        public void replaceAll(BiFunction<? super ServerPlayer, ? super SlimeRainPOI, ? extends SlimeRainPOI> function) {
            throw new RuntimeException("Attempted to run replaceAll on the playerPOIMap, which is not supported!");
        }
    };
}
