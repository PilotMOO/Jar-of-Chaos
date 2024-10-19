package mod.pilot.jar_of_chaos.entities.AI;

import mod.pilot.jar_of_chaos.entities.mobs.ChatteringTeethEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class ChatteringTeethMovementGoal extends Goal {
    final ChatteringTeethEntity parent;
    double maxLeapSpeed;
    private int nextIdleLeap;
    public LivingEntity latchedTarget;
    private double latchYDistanceFromBase;
    public ChatteringTeethMovementGoal(ChatteringTeethEntity parent, double maxSpeed){
        this.parent = parent;
        this.maxLeapSpeed = maxSpeed;
    }

    @Override
    public boolean canUse() {
        return true;
    }
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        nextIdleLeap = parent.getRandom().nextInt(10, 30);
    }

    @Override
    public void tick() {
        if (parent.getTarget() == parent.getOwner()) parent.setTarget(null);
        if (parent.getTarget() != null){
            parent.getNavigation().moveTo(parent.getTarget(), 0.0);
        }
        else if (parent.tickCount % nextIdleLeap == 0){
            Vec3 nextPos = DefaultRandomPos.getPos(parent, 16, 6);
            if (nextPos == null) return;
            parent.getNavigation().moveTo(nextPos.x, nextPos.y, nextPos.z, 0.0);
        }

        if (parent.onGround()){
            Path path = parent.getNavigation().getPath();
            if (path == null) return;
            Vec3 nextPos = path.getNextNode().asVec3();
            parent.setDeltaMovement(nextPos.subtract(parent.position()).normalize()
                    .multiply(maxLeapSpeed, 0, maxLeapSpeed).add(0, maxLeapSpeed, 0));
            path.advance();
        }
        if (!parent.onGround() && parent.getDeltaMovement().x + parent.getDeltaMovement().z / 2 < 0.05 && parent.horizontalCollision){
            parent.setDeltaMovement(parent.getDeltaMovement().add(parent.getForward()));
        }

        parent.lookAt(EntityAnchorArgument.Anchor.FEET, parent.position().add(parent.getDeltaMovement()));
    }
    private void LatchToTarget(LivingEntity target){
        System.out.println("Latching to target!");
        latchYDistanceFromBase = target.position().subtract(parent.position()).y;
        setState(2);
        latchedTarget = target;
    }
    private void BiteTarget() {
        System.out.println("Bite");
        if (latchedTarget == null || latchedTarget.isDeadOrDying()){
            setState(0);
            latchedTarget = null;
            latchYDistanceFromBase = 0;
            return;
        }
        parent.setPos(getTargetLatchPos());
        if (parent.tickCount % 20 == 0){
            parent.doHurtTarget(latchedTarget);
        }
    }

    private int getState(){
        return parent.getAIState();
    }
    private void setState(int state){
        parent.setAIState(state);
    }
    protected Vec3 getTargetLatchPos(){
        return latchedTarget.position().add(parent.getForward().reverse().multiply(
                        parent.getTarget().getBbWidth() / 2,
                        0,
                        parent.getTarget().getBbWidth() / 2))
                        .add(0, latchYDistanceFromBase, 0);
    }
}
