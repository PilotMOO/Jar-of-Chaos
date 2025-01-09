package mod.pilot.jar_of_chaos.systems.SlimeRain;

import net.minecraft.world.phys.Vec3;

public class SlimeRainPOI {
    public final Vec3 position;
    public final int range;
    private int cumulativeKills;

    public SlimeRainPOI(Vec3 position, int range) {
        this.position = position;
        this.range = range;

        SlimeRainManager.POIs.add(this);
    }
    public SlimeRainPOI(Vec3 position) {
        this(position, SlimeRainManager.POIRange);
    }

    public int getKills(){
        return cumulativeKills;
    }
    public void addKills(int count){
        if (cumulativeKills == -1) return;
        cumulativeKills += count;
    }
    public void addKill(){
        if (cumulativeKills == -1) return;
        cumulativeKills++;
    }
    public void terminate(){
        cumulativeKills = -1;
    }

    public double distance(Vec3 pos){
        return position.distanceTo(pos);
    }
    public double distanceIfInRange(Vec3 pos){
        double dist = position.distanceTo(pos);
        return dist <= range ? dist : -1;
    }
}
