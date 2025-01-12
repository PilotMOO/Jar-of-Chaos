package mod.pilot.jar_of_chaos.systems.SlimeRain;

import mod.pilot.jar_of_chaos.data.IntegerCycleTracker;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;

public class SlimeRainManager {
    private static final Random random = new Random();

    public static final int DefaultPOIRange = 256;
    public static int POIRange = DefaultPOIRange;
    public static final int DefaultNeededKills = 100;
    public static int NeededKills = DefaultNeededKills;
    private static ArrayList<SlimeRainPOI> POIs;
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
    }

    public static boolean finalizing = false;

    private static final IntegerCycleTracker.Randomized SlimeSpawnTracker = new IntegerCycleTracker.Randomized(300, 200);
    private static final IntegerCycleTracker.Randomized SlimeBallProjTracker = new IntegerCycleTracker.Randomized(300, 100);
    private static final IntegerCycleTracker.Randomized SlimeLayerTracker = new IntegerCycleTracker.Randomized(100, 80);
    private static final IntegerCycleTracker KingSlimeKillCountTracker = new IntegerCycleTracker(100);
    private static final IntegerCycleTracker BossBarUpdateTracker = new IntegerCycleTracker(60);
    private static final IntegerCycleTracker RefreshRainTracker = new IntegerCycleTracker(100);
    public static void resetAllTrackers(){
        SlimeSpawnTracker.reset();
        SlimeBallProjTracker.reset();
        SlimeLayerTracker.reset();
        KingSlimeKillCountTracker.reset();
        BossBarUpdateTracker.reset();
        RefreshRainTracker.reset();
    }

    public static void StartSlimeRain(ServerLevel server, int rainFor, int POIRangeOverride, int neededKillsOverride){
        JarGeneralSaveData.setSlimeRainDuration(rainFor);
        server.getLevelData().setRaining(true);

        POIRange = POIRangeOverride;
        NeededKills = neededKillsOverride;
        POIs = new ArrayList<>();
        finalizing = false;
    }
    public static void StartSlimeRain(ServerLevel server, int rainFor){
        StartSlimeRain(server, rainFor, DefaultPOIRange, DefaultNeededKills);
    }
    public static void TickSlimeRain(ServerLevel server){
        JarGeneralSaveData.tickSlimeRainDuration();

        if (!JarGeneralSaveData.isSlimeRain()){
            StopSlimeRain(server, finalizing);
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
            //ToDo: add bouncy slimeball projectile
            //ToDo: manage spawning bouncy slimeball projectiles around the player(s)
        }
        if (SlimeLayerTracker.tick()){
            //ToDo: add slime layer block
            //ToDo: manage spawning slime layers around the player(s)
        }
        if (KingSlimeKillCountTracker.tick()){
            ArrayList<SlimeRainPOI> validPOIs = checkAndGatherPOIsWithKillCount(NeededKills);
            if (validPOIs != null){
                for (SlimeRainPOI poi : validPOIs){
                    Vec3 pos = findSurfaceThenAdd(server, poi.position, 50);
                    KingSlimeEntity.SpawnInAt(server, pos, 200, true);
                    poi.terminate();
                    POIs.remove(poi);
                    //ToDo: broadcast message to all players
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

    public static void StopSlimeRain(@Nullable ServerLevel server, boolean force){
        if (force) finalizeSlimeRain(server, true);
        else internalGradualStopSlimeRain(server);
    }
    private static void internalGradualStopSlimeRain(@Nullable ServerLevel server){
        JarGeneralSaveData.setSlimeRainDuration(180);
        if (server != null) server.getLevelData().setRaining(false);
        finalizing = true;
    }
    private static void finalizeSlimeRain(){
        finalizeSlimeRain(null, false);
    }
    private static void finalizeSlimeRain(@Nullable ServerLevel server, boolean stopNormalRain){
        resetAllTrackers();
        for (SlimeRainPOI poi : POIs) poi.terminate();
        POIs = null;
        playerPOIMap.clear();
        POIRange = DefaultPOIRange;
        NeededKills = DefaultNeededKills;
        finalizing = false;

        JarGeneralSaveData.setSlimeRainDuration(0);
        if (stopNormalRain && server != null) server.getLevelData().setRaining(false);
        JarGeneralSaveData.Dirty();
    }

    private static Vec3 findSurfaceThenAdd(ServerLevel server, Vec3 position, int addedY) {
        BlockPos.MutableBlockPos mBPos = BlockPos.containing(position).mutable();
        BlockState bState;
        do{
            mBPos.move(0, 1, 0);
            bState = server.getBlockState(mBPos);
        }
        while (!bState.isAir() && server.canSeeSky(mBPos));
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

    public static @Nullable ArrayList<SlimeRainPOI> checkAndGatherPOIsWithKillCount(int killCount){
        ArrayList<SlimeRainPOI> valid = null;
        for (SlimeRainPOI poi : POIs){
            if (poi.getKills() >= killCount){
                if (valid == null) valid = new ArrayList<>();
                valid.add(poi);
            }
        }
        return valid;
    }

    public static final HashMap<ServerPlayer, SlimeRainPOI> playerPOIMap = new HashMap<>(){
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
