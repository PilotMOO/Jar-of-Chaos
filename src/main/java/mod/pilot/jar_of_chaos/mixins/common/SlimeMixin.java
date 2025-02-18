package mod.pilot.jar_of_chaos.mixins.common;

import mod.pilot.jar_of_chaos.systems.PlayerGeloid.GeloidManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
public abstract class SlimeMixin extends Mob implements Enemy {
    protected SlimeMixin(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Unique private Player jarOfChaos$loyalGeloid;
    @Unique private boolean jarOfChaos$isFollowingGeloid(){
        return jarOfChaos$loyalGeloid != null;
    }
    @Override
    public void aiStep() {
        super.aiStep();
        if (!GeloidManager.isAnyGeloidsActive()) return;
        if (jarOfChaos$loyalGeloid == null) {
            if (tickCount % 80 == 0) {
                Player closest = null;
                double distance = Double.MAX_VALUE;
                for (Player p : level().players()) {
                    double dist1 = p.distanceTo(this);
                    if (dist1 < 64 && dist1 < distance && GeloidManager.isActiveGeloid(p)) {
                        closest = p;
                        distance = dist1;
                    }
                }
                jarOfChaos$loyalGeloid = closest;
            }
        } else if (tickCount % 10 == 0) {
            if (this.distanceTo(jarOfChaos$loyalGeloid) > 64) {
                jarOfChaos$loyalGeloid = null;
                setTarget(null);
                return;
            }
            if (getTarget() == null){
                if (jarOfChaos$loyalGeloid.getLastHurtMob() != null
                        && jarOfChaos$loyalGeloid.tickCount - jarOfChaos$loyalGeloid.getLastHurtMobTimestamp() < 200){
                    setTarget(jarOfChaos$loyalGeloid.getLastHurtMob());
                } else if (jarOfChaos$loyalGeloid.getLastHurtByMob() != null
                        && jarOfChaos$loyalGeloid.tickCount - jarOfChaos$loyalGeloid.getLastHurtByMobTimestamp() < 200){
                    setTarget(jarOfChaos$loyalGeloid.getLastHurtByMob());
                }
            }
        }

    }

    @Inject(method = "playerTouch", at = @At(value = "HEAD"), cancellable = true)
    public void GeloidDamageNegate(Player player, CallbackInfo ci){
        if (GeloidManager.isActiveGeloid(player)){
            if (getTarget() == player) setTarget(null);
            ci.cancel();
        }
    }

    @Inject(method = "push", at = @At(value = "RETURN"))
    public void DamageTargetWhenLoyal(Entity pEntity, CallbackInfo ci){
        Mob mob = this;
        if (mob instanceof Slime s){
            if (this.jarOfChaos$isFollowingGeloid() && getTarget() == pEntity){
                if (this.isAlive() && canAttack((LivingEntity)pEntity)) {
                    int i = s.getSize();
                    if (this.distanceToSqr(pEntity) < 0.65D * 0.65D * i * i && this.hasLineOfSight(pEntity)
                            && pEntity.hurt(this.damageSources().mobAttack(this), (float)getAttributeValue(Attributes.ATTACK_DAMAGE))) {
                        this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                        this.doEnchantDamageEffects(this, pEntity);
                    }
                } else setTarget(null);
            }
        }
    }
}
