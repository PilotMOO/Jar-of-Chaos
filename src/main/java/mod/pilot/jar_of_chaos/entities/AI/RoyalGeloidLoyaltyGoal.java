package mod.pilot.jar_of_chaos.entities.AI;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

@Deprecated
public class RoyalGeloidLoyaltyGoal extends Goal {
    public final Slime slime;
    public @Nullable Player geloid;
    public @Nullable Component priorCustomName;
    public RoyalGeloidLoyaltyGoal(Mob mob){
        if (mob instanceof Slime slime1) {
            this.slime = slime1;
        } else throw new RuntimeException("ERROR! Attempted to create a new RoyalGeloidLoyaltyGoal for an entity that does not extend Slime!");
    }
    public void setGeloid(@Nullable Player player){
        geloid = player;
    }
    @Override
    public boolean canUse() {
        return geloid != null && slime.isAlive();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return geloid != null;
    }
    @Override
    public void tick() {
        if (geloid == null) return;
        if (slime.getTarget() == null){
            if (geloid.getLastHurtByMob() != null && geloid.tickCount - geloid.getLastHurtByMobTimestamp() < 200){
                slime.setTarget(geloid.getLastHurtByMob());
            }
            if (geloid.getLastHurtMob() != null && geloid.tickCount - geloid.getLastHurtMobTimestamp() < 200){
                slime.setTarget(geloid.getLastHurtMob());
            }
        }

        if (slime.tickCount % 200 == 0 && slime.distanceTo(geloid) > 64){
            stop();
        }
    }

    @Override
    public void start() {
        if (geloid == null) return;
        priorCustomName = slime.getCustomName();
        MutableComponent geloidName = Component.literal("§a").append(geloid.getName());
        slime.setCustomName(geloidName.append(Component.translatable("jar_of_chaos.loyal_slime_name")));
        geloid.displayClientMessage(Component.literal(generateRecruitmentString()), false);
    }
    @Override
    public void stop() {
        this.geloid = null;
        slime.setTarget(null);
        slime.setCustomName(priorCustomName);
    }

    private String generateRecruitmentString(){
        return switch (slime.getRandom().nextInt(5)){
            case 0 -> slime.getName().getString() + ": " + "I will protect you, my lord!";
            case 1 -> slime.getName().getString() + ": " + "My life is yours!";
            case 2 -> slime.getName().getString() + ": " + "For the crown!";
            case 3 -> slime.getName().getString() + ": " + "I will not rest until there is no one left standing in our way.";
            case 4 -> slime.getName().getString() + ": " + "I shall serve you for as long as you need me to.";
            default -> "Erm what the sigma?";
        };
    }
}
