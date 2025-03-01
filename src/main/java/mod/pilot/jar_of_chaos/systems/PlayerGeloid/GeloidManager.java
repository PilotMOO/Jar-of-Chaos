package mod.pilot.jar_of_chaos.systems.PlayerGeloid;

import mod.pilot.jar_of_chaos.data.JarMathHelper;
import mod.pilot.jar_of_chaos.items.JarItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public class GeloidManager {
    public static void Setup(){
        MinecraftForge.EVENT_BUS.addListener(GeloidManager::GeloidPacketTick);
        MinecraftForge.EVENT_BUS.addListener(GeloidManager::GeloidFallReader);
        MinecraftForge.EVENT_BUS.addListener(GeloidManager::GeloidTick);
        MinecraftForge.EVENT_BUS.addListener(GeloidManager::GeloidStretchReader);
        MinecraftForge.EVENT_BUS.addListener(GeloidManager::GeloidDeathReader);
        MinecraftForge.EVENT_BUS.addListener(GeloidManager::GeloidHurt);
        MinecraftForge.EVENT_BUS.addListener(GeloidManager::GeloidSlimeInteract);
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
        anyActive = false;
    }
    public static void addPlayerAsGeloid(Player p){
        activeGeloids.add(p);
        GeloidPacketMap.put(p, new GeloidPacket());
        anyActive = true;
    }
    public static void removePlayerFromGeloid(Player p){
        activeGeloids.remove(p);
        GeloidPacketMap.remove(p);
        anyActive = !activeGeloids.isEmpty();
    }
    private static boolean anyActive;
    public static boolean isAnyGeloidsActive(){
        return anyActive;
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
    public static void GeloidTick(LivingEvent.LivingTickEvent event){
        if (isActiveGeloid(event.getEntity())){
            Player player = (Player)event.getEntity();
            if (player.onGround()){
                GeloidPacket packet = getOrCreatePacketFor(player);
                if (player.isCrouching()){
                    float differenceSqr = packet.squishAmount - GeloidPacket.LowerStretchCap;
                    boolean flag = packet.squishAmount > GeloidPacket.LowerStretchCap && differenceSqr > 0;
                    differenceSqr *= differenceSqr;
                    packet.Stretch(flag ?
                            -0.1f * Math.abs(1 - (differenceSqr / (differenceSqr + 5)))
                            : 0);
                }
            }
            if (!player.getItemBySlot(EquipmentSlot.HEAD).is(JarItems.KING_SLIME_CROWN.get())){
                removePlayerFromGeloid(player);
            }
        }
    }
    public static void GeloidStretchReader(LivingEvent.LivingTickEvent event){
        if (isActiveGeloid(event.getEntity())){
            Player player = (Player)event.getEntity();
            if (player.onGround() || player.fallDistance < 6 || player.getDeltaMovement().y >= 0) return;
            GeloidPacket packet = getOrCreatePacketFor(player);
            //Calling packet.Stretch(float) with an argument of 0 delays the age counter without increasing the stretch--
            //therefore, preventing the stretch from overlapping and trying to reset itself during the fall
            packet.Stretch(packet.squishAmount < GeloidPacket.UpperStretchCap ?
                    Math.abs(0.01f / (1 / (GeloidPacket.UpperStretchCap - packet.squishAmount)))
                    : 0);
        }
    }
    public static void GeloidHurt(LivingHurtEvent event){
        if (isActiveGeloid(event.getEntity())){
            Player player = (Player)event.getEntity();
            DamageSource source = event.getSource();
            Vec3 from;
            if ((from = source.getSourcePosition()) != null){
                player.addDeltaMovement(JarMathHelper.getDirectionFromAToB(from, player).multiply(3, 0.75,3));
                player.level().playSound(null, player.blockPosition(), SoundEvents.SLIME_HURT, SoundSource.PLAYERS);
            }
        }
    }
    public static void GeloidDeathReader(LivingDeathEvent event){
        if (isActiveGeloid(event.getEntity())){
            removePlayerFromGeloid((Player)event.getEntity());
        }
    }
    public static void GeloidSlimeInteract(PlayerInteractEvent.EntityInteract event){
        Player player = event.getEntity();
        if (isActiveGeloid(player) && event.getTarget() instanceof Slime s){
            if ((player.isHurt() || player.getFoodData().needsFood()) && event.getItemStack().isEmpty()){
                int size = s.getSize();
                player.heal(4);
                player.getFoodData().eat(size, size);
                s.playSound(SoundEvents.HONEY_DRINK, 1f, s.getVoicePitch());
                if (--size > 0){
                    s.setSize(size, true);
                } else s.discard();
                return;
            }
            if (event.getItemStack().is(Items.SLIME_BALL)){
                s.setSize(s.getSize() + 1, true);
                event.getItemStack().shrink(1);
                s.playSound(SoundEvents.SLIME_BLOCK_PLACE, 1, s.getVoicePitch());
                if (player instanceof ServerPlayer sPlayer){
                    sPlayer.awardStat(Stats.ITEM_USED.get(Items.SLIME_BALL));
                }
            }
        }
    }
    public static void ServerEndCleaning(ServerStoppedEvent event){
        ClearGeloids();
        System.out.println("[GELOID HANDLER] Clearing out all registered Geloids!");
    }

    public static class GeloidPacket {
        public float squishAmount;
        public float expectedOversquish;
        public float activeSquishAmount;
        public float squishDuration;
        public float age;
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
        public GeloidPacket(){
            this(1f, 1f);
        }

        public void ImpactSquish(float squishAmount){
            ImpactSquish(squishAmount, Math.abs((1 - squishAmount)) + 1);
        }
        public void ImpactSquish(float squishAmount, float squishDuration){
            ImpactSquish(squishAmount, squishDuration, false);
        }
        public void ImpactSquish(float squishAmount, float squishDuration, boolean forceSet){
            if (forceSet){
                this.squishAmount = squishAmount;
            } else {
                this.squishAmount = ((this.squishAmount - 1) / 4);
                this.squishAmount += squishAmount;
            }
            this.squishAmount = Math.max(this.squishAmount, 0.1f);
            this.expectedOversquish = 1f + ((1f - squishAmount) * 0.75f);
            this.squishDuration = squishDuration;
            this.age = 0;
        }

        private static final float UpperStretchCap = 1.5f;
        private static final float LowerStretchCap = 0.5f;
        public void Stretch(float increaseAmount){
            squishAmount += increaseAmount;
            squishDuration += Math.abs(increaseAmount * 2.5);
            age -= 1 / (squishDuration * 20);
        }
        public float LerpSquish(boolean set){
            return LerpSquish(activeSquishAmount, set);
        }
        public float LerpSquish(float old, boolean set){
            if (age >= 0.5 && set){
                squishAmount = expectedOversquish;
                expectedOversquish = 1;
                age = 0;
                if (squishAmount != 1){
                    squishDuration = Math.abs((1 - squishAmount) * 2.5f) + 1;
                } else{
                    squishDuration = Math.abs((1 - activeSquishAmount) * 2.5f) + 1;
                }
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
