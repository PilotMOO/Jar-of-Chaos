package mod.pilot.jar_of_chaos.entities.AI;

import mod.pilot.jar_of_chaos.damagetypes.JarDamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FishProjectileGoal extends Goal {
    private final LivingEntity parent;
    private Vec3 velocity;
    private boolean finished = false;
    public FishProjectileGoal(LivingEntity parent, Vec3 velocity){
        this.parent = parent;
        this.velocity = velocity;
    }
    @Override
    public boolean canUse() {
        return !parent.onGround() && !finished;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        velocity = velocity.scale(0.99);
        parent.setDeltaMovement(velocity);
        AABB KBaabb = parent.getBoundingBox().inflate(1.5);
        Vec3 newVelocity = parent.getDeltaMovement().multiply(1, 0, 1).add(0, 1, 0);
        for (Entity E : parent.level().getEntities(parent, KBaabb)){
            E.setDeltaMovement(newVelocity);
            E.hurt(JarDamageTypes.fished(parent), getAverageVelocity());
            velocity.scale(0.5);
        }

        if (parent.onGround() || parent.horizontalCollision || getAverageVelocity() < 1){
            stop();
        }
    }

    @Override
    public void stop() {
        finished = true;
    }

    public float getAverageVelocity(){
        Vec3 delta = parent.getDeltaMovement();
        return (float) ((Math.abs(delta.x) + Math.abs(delta.y) + Math.abs(delta.z)) / 3);
    }
}
