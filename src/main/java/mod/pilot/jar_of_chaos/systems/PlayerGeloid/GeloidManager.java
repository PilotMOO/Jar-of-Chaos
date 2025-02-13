package mod.pilot.jar_of_chaos.systems.PlayerGeloid;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class GeloidManager {
    public static void Register(){
        MinecraftForge.EVENT_BUS.addListener(GeloidManager::GeloidPacketTick);
        MinecraftForge.EVENT_BUS.addListener(GeloidManager::GeloidFallReader);
        MinecraftForge.EVENT_BUS.addListener(GeloidManager::ServerEndCleaning);
    }

    private static final ArrayList<Player> activeGeloids = new ArrayList<>();
    private static final HashMap<Player, GeloidPacket> GeloidPacketMap = new HashMap<>();
    public static @Nullable GeloidPacket getPacketFor(Player p){
        return GeloidPacketMap.getOrDefault(p, null);
    }
    public static @NotNull GeloidManager.GeloidPacket getOrCreatePacketFor(Player p){
        return GeloidPacketMap.computeIfAbsent(p, (P) -> new GeloidPacket(1));
    }
    private static ArrayList<GeloidPacket> getAllPackets(){
        ArrayList<GeloidPacket> packets = new ArrayList<>();
        for (Player p : getGeloids()){
            GeloidPacket packet = getPacketFor(p);
            if (packet != null) packets.add(packet);
        }
        return packets;
    }
    private static void ClearGeloids(){
        activeGeloids.clear();
        GeloidPacketMap.clear();
    }
    public static void addPlayerAsGeloid(Player p){
        activeGeloids.add(p);
        GeloidPacketMap.put(p, new GeloidPacket(1));
    }
    public static void removePlayerFromGeloid(Player p){
        activeGeloids.remove(p);
        GeloidPacketMap.remove(p);
    }
    public static ArrayList<Player> getGeloids(){
        return new ArrayList<>(activeGeloids);
    }
    public static boolean isActiveGeloid(Player p){
        return getGeloids().contains(p);
    }
    public static boolean isActiveGeloid(LivingEntity l){
        return l instanceof Player p && isActiveGeloid(p);
    }

    public static void GeloidPacketTick(TickEvent.ServerTickEvent event){
        for (GeloidPacket packet : getAllPackets()){
            if (packet.squishAmount != -1){
                packet.Age();
                System.out.println(packet);
            }
        }
    }
    public static void GeloidFallReader(LivingFallEvent event){
        if (isActiveGeloid(event.getEntity())){
            Player player = (Player)event.getEntity();
            GeloidPacket packet = getOrCreatePacketFor(player);
            double distanceSqr = event.getDistance() * event.getDistance();
            packet.ImpactSquish((float)(1 - (distanceSqr / (distanceSqr + 100))));
        }
    }
    public static void ServerEndCleaning(ServerStoppedEvent event){
        ClearGeloids();
    }

    public static class GeloidPacket {
        public float squishAmount;
        public float expectedOversquish;
        public float activeSquishAmount;
        public float squishDuration;
        private float age;
        public GeloidPacket(float squishAmount, float expectedOversquish){
            this.squishAmount = squishAmount;
            this.expectedOversquish = expectedOversquish;
            activeSquishAmount = 1f;
            squishDuration = 0;
            age = 0;
        }
        public GeloidPacket(float squishAmount){
            this(squishAmount, 1f);
        }

        public void ImpactSquish(float squishAmount){
            ImpactSquish(squishAmount, squishAmount * 2.5f);
        }
        public void ImpactSquish(float squishAmount, float squishDuration){
            this.squishAmount = (this.squishAmount - 1) / 4;
            this.squishAmount += squishAmount;
            this.expectedOversquish = 1f + ((1f - squishAmount) * 0.75f);
            this.squishDuration = squishDuration;
            this.age = 0;
        }
        public void Stretch(float increaseAmount){
            squishAmount += increaseAmount;
            if (expectedOversquish != 1){
                expectedOversquish -= increaseAmount / 4;
            }
            squishDuration += increaseAmount;
        }
        public float LerpSquish(boolean set){
            return LerpSquish(activeSquishAmount, set);
        }
        public float LerpSquish(float old, boolean set){
            if (age >= 0.5 && set){
                squishAmount = expectedOversquish;
                expectedOversquish = 1;
                age = 0;
                squishDuration = squishAmount * 2.5f;
            }
            float value = old + ((squishAmount - old) * age);
            if (set) activeSquishAmount = value;
            return value;
        }

        public void Age(){
            age += 1 / (squishDuration * 20);
            if (age > 1) age = 1;
        }

        @Override
        public String toString() {
            return "GeloidPacket [squishAmount : " + squishAmount
                    + ", expectedOversquish : " + expectedOversquish
                    + ", activeSquishAmount : " + activeSquishAmount
                    + ", squishDuration : " + squishDuration
                    + ", age : " + age
                    + ", Lerped : " + LerpSquish(false) + "]";
        }
    }
}
