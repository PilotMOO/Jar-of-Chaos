package mod.pilot.jar_of_chaos.systems.PlayerSlimeoid;

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

public class SlimeoidManager {
    public static void Register(){
        MinecraftForge.EVENT_BUS.addListener(SlimeoidManager::SlimeoidPacketTick);
        MinecraftForge.EVENT_BUS.addListener(SlimeoidManager::SlimeoidFallReader);
        MinecraftForge.EVENT_BUS.addListener(SlimeoidManager::ServerEndCleaning);
    }

    private static final ArrayList<Player> activeSlimeoids = new ArrayList<>();
    private static final HashMap<Player, SlimeoidPacket> slimeoidPacketMap = new HashMap<>();
    public static @Nullable SlimeoidPacket getPacketFor(Player p){
        return slimeoidPacketMap.getOrDefault(p, null);
    }
    public static @NotNull SlimeoidPacket getOrCreatePacketFor(Player p){
        return slimeoidPacketMap.computeIfAbsent(p, (P) -> new SlimeoidPacket(1));
    }
    private static ArrayList<SlimeoidPacket> getAllPackets(){
        ArrayList<SlimeoidPacket> packets = new ArrayList<>();
        for (Player p : getSlimeoids()){
            SlimeoidPacket packet = getPacketFor(p);
            if (packet != null) packets.add(packet);
        }
        return packets;
    }
    private static void ClearSlimeoids(){
        activeSlimeoids.clear();
        slimeoidPacketMap.clear();
    }
    public static void addPlayerAsSlimeoid(Player p){
        activeSlimeoids.add(p);
        slimeoidPacketMap.put(p, new SlimeoidPacket(1));
    }
    public static void removePlayerFromSlimeoid(Player p){
        activeSlimeoids.remove(p);
        slimeoidPacketMap.remove(p);
    }
    public static ArrayList<Player> getSlimeoids(){
        return new ArrayList<>(activeSlimeoids);
    }
    public static boolean isActiveSlimeoid(Player p){
        return getSlimeoids().contains(p);
    }
    public static boolean isActiveSlimeoid(LivingEntity l){
        return l instanceof Player p && isActiveSlimeoid(p);
    }

    public static void SlimeoidPacketTick(TickEvent.ServerTickEvent event){
        for (SlimeoidPacket packet : getAllPackets()){
            if (packet.squishAmount != -1){
                packet.Age();
                System.out.println(packet);
            }
        }
    }
    public static void SlimeoidFallReader(LivingFallEvent event){
        if (isActiveSlimeoid(event.getEntity())){
            Player player = (Player)event.getEntity();
            SlimeoidPacket packet = getOrCreatePacketFor(player);
            double distanceSqr = event.getDistance() * event.getDistance();
            packet.ImpactSquish((float)(1 - (distanceSqr / (distanceSqr + 100))));
        }
    }
    public static void ServerEndCleaning(ServerStoppedEvent event){
        ClearSlimeoids();
    }

    public static class SlimeoidPacket{
        public float squishAmount;
        public float expectedOversquish;
        public float activeSquishAmount;
        public float squishDuration;
        private float age;
        public SlimeoidPacket(float squishAmount, float expectedOversquish){
            this.squishAmount = squishAmount;
            this.expectedOversquish = expectedOversquish;
            activeSquishAmount = 1f;
            squishDuration = 0;
            age = 0;
        }
        public SlimeoidPacket(float squishAmount){
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
            return "SlimeoidPacket [squishAmount : " + squishAmount
                    + ", expectedOversquish : " + expectedOversquish
                    + ", activeSquishAmount : " + activeSquishAmount
                    + ", squishDuration : " + squishDuration
                    + ", age : " + age
                    + ", Lerped : " + LerpSquish(false) + "]";
        }
    }
}
