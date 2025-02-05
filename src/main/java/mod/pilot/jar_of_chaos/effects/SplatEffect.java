package mod.pilot.jar_of_chaos.effects;

import mod.pilot.jar_of_chaos.sound.JarSounds;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class SplatEffect extends MobEffect {
    public SplatEffect() {
        super(MobEffectCategory.NEUTRAL, 10151823);
    }

    private static final HashMap<LivingEntity, Vec3> entityDeltaMap = new HashMap<>();
    public static void Flush() {
        entityDeltaMap.clear();
    }
    private Vec3 getDeltaOf(LivingEntity target){
        return entityDeltaMap.get(target);
    }
    private void updateDelta(LivingEntity target){
        if (entityDeltaMap.containsKey(target)) entityDeltaMap.replace(target, target.getDeltaMovement());
        else entityDeltaMap.put(target, target.getDeltaMovement());
    }
    private Vec3 getThenUpdateDelta(LivingEntity target){
        Vec3 toReturn = getDeltaOf(target);
        updateDelta(target);
        return toReturn;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity target, int amp) {
        MobEffectInstance instance = target.getEffect(this);
        if (instance == null) return;
        int duration = instance.getDuration();
        if (duration <= 1 && duration != -1){
            entityDeltaMap.remove(target);
            return;
        }

        if (!entityDeltaMap.containsKey(target) || (!target.horizontalCollision && !target.verticalCollision)) {
            if (target.verticalCollisionBelow) target.addDeltaMovement(new Vec3(0, target.getRandom().nextDouble() + 0.5, 0));
            updateDelta(target);
            return;
        }

        Vec3 oldDelta = getThenUpdateDelta(target);
        double difference = getAbsolute(oldDelta) - getAbsolute(target.getDeltaMovement());
        if (difference > 0.25) {
            target.hurt(target.damageSources().flyIntoWall(), (float) (difference * (5 + (2.5 * amp))));
            Direction direction = Direction.getNearest(oldDelta.x, oldDelta.y, oldDelta.z);
            Vec3 scale = new Vec3(direction.step().absolute().negate()).scale(2).add(1, 1, 1).scale(0.75f);
            target.setDeltaMovement(oldDelta.multiply(scale));
            target.playSound(JarSounds.BOING.get());
        }
    }

    private static double getAbsolute(Vec3 vector){
        return Math.sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration > 0 || pDuration == -1;
    }
}
