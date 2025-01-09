package mod.pilot.jar_of_chaos.systems.SlimeRain;

import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class SlimeRainManager {
    public static final int DefaultPOIRange = 128;
    public static int POIRange = DefaultPOIRange;
    public static ArrayList<SlimeRainPOI> POIs;

    private static final IntCycleTracker.Randomized SlimeSpawnTracker = new IntCycleTracker.Randomized(200, 100);
    private static final IntCycleTracker.Randomized SlimeBallProjTracker = new IntCycleTracker.Randomized(300, 100);
    private static final IntCycleTracker.Randomized SlimeLayerTracker = new IntCycleTracker.Randomized(100, 80);

    public static void StartSlimeRain(ServerLevel server, int rainFor, int POIRangeOverride){
        JarGeneralSaveData.setSlimeRainDuration(rainFor);
        if (!server.getLevelData().isRaining()) server.getLevelData().setRaining(true);

        POIRange = POIRangeOverride;
        POIs = new ArrayList<>();
    }
    public static void StartSlimeRain(ServerLevel server, int rainFor){
        StartSlimeRain(server, rainFor, DefaultPOIRange);
    }
    public static void TickSlimeRain(){
        JarGeneralSaveData.tickSlimeRainDuration();

        if (SlimeSpawnTracker.tick()){
            //ToDo: manage spawning slimes around the player(s)
        }
        if (SlimeBallProjTracker.tick()){
            //ToDo: add bouncy slimeball projectile
            //ToDo: manage spawning bouncy slimeball projectiles around the player(s)
        }
        if (SlimeLayerTracker.tick()){
            //ToDo: add slime layer block
            //ToDo: manage spawning slime layers around the player(s)
        }
    }
    public static void FinalizeSlimeRain(){
        POIs = null;
        POIRange = DefaultPOIRange;
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

    public static boolean checkPOIsForKillCount(int killCount){
        for (SlimeRainPOI poi : POIs){
            if (poi.getKills() >= killCount) return true;
        }
        return false;
    }

    private static class IntCycleTracker {
        public final int MAX;
        public int tracker;
        private IntCycleTracker(int max){
            this.MAX = max;
            tracker = 0;
        }

        public boolean tick(){
            if (tracker < 0) tracker = MAX;
            return --tracker == 0;
        }

        public void reset(){
            tracker = 0;
        }

        public static class Randomized extends IntCycleTracker{
            private static final Random random = new Random();
            public final int clamp;
            public Randomized(int middle, int clamp){
                super(middle);
                this.clamp = clamp;
            }

            @Override
            public boolean tick() {
                if (tracker < 0) tracker = MAX + random.nextInt(-clamp, clamp);
                return --tracker == 0;
            }
        }
    }
}
