package mod.pilot.jar_of_chaos.systems.SlimeRain;

import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.phys.Vec3;

public class SlimeRainPOI {
    public final Vec3 position;
    public final int range;
    private int cumulativeKills;

    public SlimeRainPOI(Vec3 position, int range) {
        this.position = position;
        this.range = range;

        this.SlimePOIBossEvent = new ServerBossEvent(Component.literal("Slime Rain Progress"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        SlimeRainManager.AddToPOIs(this);
    }
    public SlimeRainPOI(Vec3 position) {
        this(position, SlimeRainManager.POIRange);
    }
    public static SlimeRainPOI createFromBlueprint(int kills, int range, Vec3 pos){
        return new SlimeRainPOI(pos, range, kills);
    }
    private SlimeRainPOI(Vec3 position, int range, int kills){
        this.position = position;
        this.range = range;
        this.cumulativeKills = kills;

        this.SlimePOIBossEvent = new ServerBossEvent(Component.literal("Slime Rain Progress"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
        updateBossEvent();
    }


    public int getKills(){
        return cumulativeKills;
    }
    public void addKills(int count){
        if (cumulativeKills == -1) return;
        cumulativeKills += count;
        updateBossEvent();
        JarGeneralSaveData.Dirty();
    }
    public void addKill(){
        if (cumulativeKills == -1) return;
        cumulativeKills++;
        updateBossEvent();
        JarGeneralSaveData.Dirty();
    }
    public void terminate(){
        cumulativeKills = -1;
        SlimePOIBossEvent.setVisible(false);
        SlimePOIBossEvent.removeAllPlayers();
        JarGeneralSaveData.Dirty();
    }

    public double distance(Vec3 pos){
        return position.distanceTo(pos);
    }
    public double distanceIfInRange(Vec3 pos){
        double dist = position.distanceTo(pos);
        return dist <= range ? dist : -1;
    }

    private final ServerBossEvent SlimePOIBossEvent;
    public void addPlayerToBossEvent(ServerPlayer player){
        SlimePOIBossEvent.addPlayer(player);
    }
    public void removePlayerFromBossEvent(ServerPlayer player){
        SlimePOIBossEvent.removePlayer(player);
    }
    private void updateBossEvent(){
        SlimePOIBossEvent.setProgress((float) getKills() / SlimeRainManager.NeededKills);
    }
}
