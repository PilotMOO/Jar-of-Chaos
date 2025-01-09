package mod.pilot.jar_of_chaos.entities.mobs;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.pilot.jar_of_chaos.Config;
import mod.pilot.jar_of_chaos.damagetypes.JarDamageTypes;
import mod.pilot.jar_of_chaos.items.JarItems;
import mod.pilot.jar_of_chaos.sound.JarSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class ChatteringTeethEntity extends PathfinderMob implements GeoEntity {
    public ChatteringTeethEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new ChatteringTeethMoveControl(this);
    }

    private static final boolean strangerDanger = Config.SERVER.pickup_teeth.get();
    private static final boolean TeethPVP = Config.SERVER.teeth_pvp.get();
    private static final int teethMaxAge = Config.SERVER.teeth_age.get();

    public enum state{
        idle,
        chase,
        latched
    }
    public static final EntityDataAccessor<Integer> AIState = SynchedEntityData.defineId(ChatteringTeethEntity.class, EntityDataSerializers.INT);
    public int getAIState(){return entityData.get(AIState);}
    public void setAIState(int count) {
        entityData.set(AIState, count);
        if (count == 2){
            RandomZRot();
        }
        else{
            setZLatchRotate(0);
        }
    }
    public void setAIState(state ordinal) {setAIState(ordinal.ordinal());}
    public static final EntityDataAccessor<Integer> Age = SynchedEntityData.defineId(ChatteringTeethEntity.class, EntityDataSerializers.INT);
    public int getAge(){return entityData.get(Age);}
    public void setAge(int age) {entityData.set(Age, age);}
    public void AgeBy1() {setAge(getAge() + 1);}
    public static final EntityDataAccessor<Optional<UUID>> Owner = SynchedEntityData.defineId(ChatteringTeethEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public @Nullable UUID getOwnerUUID(){return entityData.get(Owner).orElse(null);}
    private Entity owner;
    public @Nullable Entity getOwner(){
        if (owner != null) return owner;
        UUID uuid = getOwnerUUID();
        if (uuid == null) return null;
        return level() instanceof ServerLevel server ? owner = server.getEntity(uuid) : null;
    }
    public void setOwnerUUID(UUID uuid) {entityData.set(Owner, Optional.of(uuid));}
    public void setOwnerUUID(Entity owner){
        setOwnerUUID(owner.getUUID());
        this.owner = owner;
    }
    public static final EntityDataAccessor<Optional<UUID>> LatchTarget = SynchedEntityData.defineId(ChatteringTeethEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    public @Nullable UUID getLatchTargetUUID(){
        return entityData.get(LatchTarget).orElse(null);}
    private LivingEntity latchTarget;
    public @Nullable LivingEntity getLatchTarget(){
        if (latchTarget != null) return latchTarget;
        UUID uuid = getOwnerUUID();
        if (uuid == null) return null;
        return level() instanceof ServerLevel server ? latchTarget = (LivingEntity)server.getEntity(uuid) : null;
    }
    public void setLatchTargetUUID(@Nullable UUID uuid) {
        if (uuid == null){
            entityData.set(LatchTarget, Optional.empty());
            return;
        }
        entityData.set(LatchTarget, Optional.of(uuid));
    }
    public void setLatchTargetUUID(LivingEntity latchTarget){
        setOwnerUUID(latchTarget.getUUID());
        this.latchTarget = latchTarget;
    }
    public static final EntityDataAccessor<Float> ZLatchRotate = SynchedEntityData.defineId(ChatteringTeethEntity.class, EntityDataSerializers.FLOAT);
    public float getZLatchRotate(){return entityData.get(ZLatchRotate);}
    public void setZLatchRotate(float rot) {entityData.set(ZLatchRotate, rot);}
    public void RandomZRot(){
        setZLatchRotate(random.nextInt(-60, 61));
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AIState",entityData.get(AIState));
        tag.putInt("Age",entityData.get(Age));
        UUID Ouuid = getOwnerUUID();
        if (Ouuid != null) {
            tag.putUUID("Owner", Ouuid);
        }
        UUID Luuid = getLatchTargetUUID();
        if (Luuid != null) {
            tag.putUUID("LatchTarget", Luuid);
        }
        tag.putFloat("ZLatchRotate", entityData.get(ZLatchRotate));
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(AIState, tag.getInt("AIState"));
        entityData.set(Age, tag.getInt("Age"));
        if (tag.contains("Owner")){
            UUID Ouuid = tag.getUUID("Owner");
            entityData.set(Owner, Optional.of(Ouuid));
            if (level() instanceof ServerLevel s){
                owner = s.getEntity(Ouuid);
            }
        }
        if (tag.contains("LatchTarget")){
            UUID Luuid = tag.getUUID("LatchTarget");
            entityData.set(LatchTarget, Optional.of(Luuid));
            if (level() instanceof ServerLevel s){
                latchTarget = (LivingEntity)s.getEntity(Luuid);
            }
        }
        entityData.set(ZLatchRotate, tag.getFloat("ZLatchRotate"));
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AIState, 0);
        this.entityData.define(Age, 0);
        this.entityData.define(Owner, Optional.empty());
        this.entityData.define(LatchTarget, Optional.empty());
        this.entityData.define(ZLatchRotate, 0f);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "CrashManager", event -> {
            switch (getAIState()){
                default ->{
                    return event.setAndContinue(RawAnimation.begin().thenLoop("Chatter"));
                }
                case 1 ->{
                    return event.setAndContinue(RawAnimation.begin().thenLoop("Chase"));
                }
                case 2 ->{
                    return event.setAndContinue(RawAnimation.begin().thenLoop("Bite"));
                }
            }
        }));
    }

    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false,
                this::ChatterLatchPredicate));
        this.goalSelector.addGoal(1, new ChatteringTeethChaseAndLatchGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes(){
        return ChatteringTeethEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 4D)
                .add(Attributes.ARMOR, 1)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.MOVEMENT_SPEED, 1D)
                .add(Attributes.ATTACK_DAMAGE, 2D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D)
                .add(Attributes.ATTACK_SPEED, 2D);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, @NotNull DamageSource pSource) {
        return false;
    }

    @Override
    public void awardKillScore(@NotNull Entity pKilled, int pScoreValue, @NotNull DamageSource pSource) {
        if (getOwner() != null){
            getOwner().awardKillScore(pKilled, pScoreValue, pSource);
            return;
        }
        super.awardKillScore(pKilled, pScoreValue, pSource);
    }

    @Override
    public void tick() {
        if (tickCount % 55 == 1 && level() instanceof ServerLevel server){
            server.playSound(null, this, JarSounds.CHATTERING_TEETH.get(), SoundSource.HOSTILE, 1, 1);
        }
        super.tick();

        setNoGravity(getAIState() == 2);

        if (getAge() > teethMaxAge){
            if (getOwner() instanceof Player){
                ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(),
                        new ItemStack(JarItems.CHATTERING_TEETH_SPAWN.get()));
                item.setDeltaMovement(0, 0.25, 0);
                level().addFreshEntity(item);
            }
            level().playSound(null, this, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1f, 0.5f);
            discard();
            return;
        }
        AgeBy1();
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (tickCount < 5) return InteractionResult.FAIL;
        if (player.isCreative() || player == getOwner() || strangerDanger){
            ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(),
                    new ItemStack(JarItems.CHATTERING_TEETH_SPAWN.get()));
            item.setDeltaMovement(0, 0.25, 0);
            level().addFreshEntity(item);
            this.discard();
            level().playSound(null, this, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1f, 0.5f);
        }
        else{
            setTarget(null);
            setAIState(0);
            setLatchTargetUUID((UUID)null);
            setDeltaMovement(player.getForward());
            level().playSound(null, this, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1f, 1.5f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void pushEntities() {}

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        if (entity == getOwner() || !(entity instanceof LivingEntity)) return false;
        boolean flag = entity.hurt(JarDamageTypes.teeth(this), (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        this.setLastHurtMob(entity);
        if (flag && getAIState() == 2) {
            entity.invulnerableTime = 0;
            entity.setDeltaMovement(getDeltaMovement().scale(0.75));
        }
        return flag;
    }

    private boolean ChatterLatchPredicate(LivingEntity target){
        if (target instanceof ArmorStand) return false;
        if (target == getOwner()) return false;
        if (getOwner() instanceof Player && target instanceof Player) return TeethPVP;
        if (target instanceof TamableAnimal TA){
            LivingEntity TAOwner = TA.getOwner();
            if (TAOwner == null) return true;
            return TA.getOwner() != getOwner();
        }
        if (target instanceof ChatteringTeethEntity CTE){
            if (getOwner() == null && CTE.getOwner() == null) return false;
            else{
                return getOwner() != CTE.getOwner();
            }
        }
        return true;
    }

    //Stolen (then modified) from the slime class because it was private :/
    static class ChatteringTeethMoveControl extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final ChatteringTeethEntity teeth;
        private boolean isAggressive;

        public ChatteringTeethMoveControl(ChatteringTeethEntity teeth) {
            super(teeth);
            this.teeth = teeth;
            this.yRot = 180.0F * teeth.getYRot() / (float)Math.PI;
        }

        public void setDirection(float pYRot, boolean pAggressive) {
            this.yRot = pYRot;
            this.isAggressive = pAggressive;
        }

        public void setWantedMovement(double pSpeed) {
            this.speedModifier = pSpeed;
            this.operation = MoveControl.Operation.MOVE_TO;
        }

        public void tick() {
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0F));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setZza(0.0F);
            } else {
                this.operation = MoveControl.Operation.WAIT;
                if (this.mob.onGround()) {
                    this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * getSpeedFactorFromTarget(teeth.getTarget())));
                    if (this.jumpDelay-- <= 0) {
                        this.jumpDelay = getJumpDelay();
                        if (this.isAggressive) {
                            this.jumpDelay /= 3;
                        }

                        this.teeth.getJumpControl().jump();
                    } else {
                        this.teeth.xxa = 0.0F;
                        this.teeth.zza = 0.0F;
                        this.mob.setSpeed(0.0F);
                    }
                } else {
                    this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * getSpeedFactorFromTarget(teeth.getTarget())));
                }

            }
        }

        private double getSpeedFactorFromTarget(LivingEntity target) {
            if (target == null) return 1;
            double distance = teeth.distanceTo(target);
            return distance > 2 ? 1 : Math.min(1 / (2 - distance), 0) + 0.25;
        }

        protected int getJumpDelay() {
            return 5;
        }
    }
    static class ChatteringTeethChaseAndLatchGoal extends Goal {
        private final ChatteringTeethEntity teeth;
        private double latchYDistanceFromBase;
        private LivingEntity latchedTarget;

        public ChatteringTeethChaseAndLatchGoal(ChatteringTeethEntity teeth) {
            this.teeth = teeth;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
            LivingEntity latched = teeth.getLatchTarget();
            if (latched != null){
                LatchToTarget(latched);
                teeth.setTarget(latched);
            }
        }
        public boolean canUse() {
            LivingEntity livingentity = this.teeth.getTarget();
            if (livingentity == null) {
                Detach();
                return false;
            } else {
                return this.teeth.canAttack(livingentity) && this.teeth.getMoveControl() instanceof ChatteringTeethMoveControl;
            }
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = this.teeth.getTarget();
            if (livingentity == null) {
                return false;
            }
            return teeth.canAttack(livingentity);
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity target = this.teeth.getTarget();
            if (target != null) {
                if (teeth.getAIState() != 2){
                    teeth.setAIState(1);
                    this.teeth.lookAt(target, 10.0F, 10.0F);
                    if (teeth.tickCount % 10 == 0){
                        teeth.getNavigation().moveTo(target, 1);
                    }
                    if (teeth.distanceTo(target) < (teeth.getBbWidth() * 1.2) + target.getBbWidth()){
                        LatchToTarget(target);
                    }
                }
                else{
                    if (target != latchedTarget || latchedTarget.isDeadOrDying()){
                        Detach();
                        return;
                    }
                    this.teeth.lookAt(target, 10.0F, 10.0F);
                    teeth.setPos(getTargetLatchPos());
                    teeth.resetFallDistance();
                    if (teeth.tickCount % 20 == 0){
                        teeth.doHurtTarget(latchedTarget);
                    }
                }
            }
            else{
                if (latchedTarget != null){
                    Detach();
                    return;
                }
                teeth.setAIState(0);
            }

            MoveControl movecontrol = this.teeth.getMoveControl();
            if (movecontrol instanceof ChatteringTeethMoveControl teethMoveControl) {
                teethMoveControl.setDirection(this.teeth.getYRot(), teeth.isAggressive());
            }
        }

        private void LatchToTarget(LivingEntity target) {
            latchYDistanceFromBase = target.position().y - target.position().y;
            double targetHeight = target.getBbHeight();
            if (latchYDistanceFromBase < targetHeight / 4 && targetHeight > 1){
                latchYDistanceFromBase += teeth.getRandom().nextDouble() * (target.getBbHeight() - latchYDistanceFromBase);
            }
            teeth.setAIState(2);
            latchedTarget = target;
        }

        private void Detach() {
            teeth.setAIState(0);
            teeth.setTarget(null);
            latchYDistanceFromBase = 0;
            latchedTarget = null;
            teeth.setZLatchRotate(0);
            teeth.getNavigation().moveTo(teeth, 1);
        }

        protected Vec3 getTargetLatchPos(){
            return latchedTarget.position().add(teeth.getForward().reverse().multiply(
                            latchedTarget.getBbWidth() / 4,
                            0,
                            latchedTarget.getBbWidth() / 4))
                    .add(0, latchYDistanceFromBase, 0);
        }
    }
}
