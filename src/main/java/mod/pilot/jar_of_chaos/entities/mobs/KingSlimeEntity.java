package mod.pilot.jar_of_chaos.entities.mobs;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.pilot.jar_of_chaos.data.IntegerCycleTracker;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.systems.SlimeRain.KingSlimeBossEventManager;
import mod.pilot.jar_of_chaos.systems.SlimeRain.SlimeRainManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class KingSlimeEntity extends PathfinderMob implements GeoEntity {
    private final KingSlimeEntity.BossEvent bossEvent;
    public KingSlimeEntity.BossEvent getBossEvent(){return bossEvent;}
    public UUID getBossEventID(){return bossEvent.getId();}
    private boolean wasOnGround;
    private int sweat = 0;
    public KingSlimeEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.fixupDimensions();
        this.moveControl = new KingSlimeMoveControl(this, 2);
        bossEvent = new BossEvent(this.getName(), getSize());
    }
    private KingSlimeEntity(Level pLevel, int size) {
        super(JarEntities.KING_SLIME.get(), pLevel);
        this.fixupDimensions();
        this.moveControl = new KingSlimeMoveControl(this, 2);
        bossEvent = new BossEvent(this.getName(), size);
        this.setSize(size);
    }

    public static void SpawnInAt(ServerLevel server, Vec3 pos, int startingSize, boolean fromSlimeRain){
        KingSlimeEntity kSlime = new KingSlimeEntity(server, startingSize);
        kSlime.moveTo(pos);
        kSlime.setFromSlimeRain(fromSlimeRain);
        server.addFreshEntity(kSlime);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide() && !KingSlimeBossEventManager.getActiveKingSlimes().contains(this)){
            KingSlimeBossEventManager.addToList(this);
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!level().isClientSide() && KingSlimeBossEventManager.getActiveKingSlimes().contains(this)){
            KingSlimeBossEventManager.removeFromList(this);
        }
        bossEvent.removeAllPlayers();
        bossEvent.setVisible(false);
    }

    /*private enum State{
                idle,
                jumping,
                landing;
                public int toInt(){
                    return this.ordinal();
                }
                public static @Nullable State fromInt(int i){
                    return switch (i){
                        case 0 -> idle;
                        case 1 -> jumping;
                        case 2 -> landing;
                        default -> null;
                    };
                }
            }
            public static final EntityDataAccessor<Integer> AIState = SynchedEntityData.defineId(KingSlimeEntity.class, EntityDataSerializers.INT);
            public int getState(){return entityData.get(AIState);}
            public void setState(int state) {entityData.set(AIState, state);}*/
    public static final EntityDataAccessor<Boolean> Fleeing = SynchedEntityData.defineId(KingSlimeEntity.class, EntityDataSerializers.BOOLEAN);
    public boolean isFleeing(){return entityData.get(Fleeing);}
    public void setFleeing(boolean flag){entityData.set(Fleeing, flag);}
    public static final EntityDataAccessor<Boolean> FromSlimeRain = SynchedEntityData.defineId(KingSlimeEntity.class, EntityDataSerializers.BOOLEAN);
    public boolean isFromSlimeRain(){return entityData.get(FromSlimeRain);}
    public void setFromSlimeRain(boolean flag){entityData.set(FromSlimeRain, flag);}
    public static final EntityDataAccessor<Integer> Size = SynchedEntityData.defineId(KingSlimeEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> LargestSize = SynchedEntityData.defineId(KingSlimeEntity.class, EntityDataSerializers.INT);
    public int getSize(){return entityData.get(Size);}
    public int getLargestSize(){return entityData.get(LargestSize);}
    public void setSize(int size) {
        entityData.set(Size, size);
        this.reapplyPosition();
        this.refreshDimensions();
        UpdateStatsBySize();
        if (size > bossEvent.MaxProgress) {
            bossEvent.MaxProgress = size;
            entityData.set(LargestSize, size);
        }
        bossEvent.generateAndSetProgress(getSize());
    }
    public void forceSetLargestSize(int size){
        entityData.set(LargestSize, size);
    }
    public float getSizeScale(){
        return getSizeScale(0.075f);
    }
    public float getSizeScale(float factor){
        return 1f + (getSize() * factor);
    }
    public void sizeUpBy(int count) {
        setSize(getSize() + count);
    }
    public void sizeDownBy(int count) {
        setSize(getSize() - count);
    }
    public void UpdateStatsBySize(){
        AttributeInstance mHealth = getAttribute(Attributes.MAX_HEALTH);
        if (mHealth != null){
            double priorCap = mHealth.getBaseValue();
            mHealth.setBaseValue(getSize());
            if (priorCap < mHealth.getBaseValue()){
                heal((float)(mHealth.getBaseValue() - priorCap));
            }
        }

        AttributeInstance mSpeed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (mSpeed != null) mSpeed.setBaseValue(getSizeScale(0.005f) / 2);

        AttributeInstance jSpeed = getAttribute(Attributes.JUMP_STRENGTH);
        if (jSpeed != null) jSpeed.setBaseValue(getSizeScale(0.05f));

        AttributeInstance attack = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.setBaseValue(4 + getSizeScale(0.025f));

        AttributeInstance kb = getAttribute(Attributes.ATTACK_KNOCKBACK);
        if (kb != null) kb.setBaseValue(getSize() * 0.1f);

        AttributeInstance kbRes = getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kbRes != null) kbRes.setBaseValue(getSizeScale(0.0025f) - 1);
    }
    public static final EntityDataAccessor<Integer> TeleportTimer = SynchedEntityData.defineId(KingSlimeEntity.class, EntityDataSerializers.INT);
    public int getTeleportTimer() { return entityData.get(TeleportTimer); }
    public void setTeleportTimer(int timer) { entityData.set(TeleportTimer, timer); }
    public boolean TickTeleport(){
        int timer = getTeleportTimer();
        if (timer <= 0){
            setTeleportTimer(-1);
            return true;
        } else {
            setTeleportTimer(getTeleportTimer() - 1);
            return false;
        }
    }
    public Vec3 teleportPosition;
    public static final EntityDataAccessor<Boolean> IsTeleporting = SynchedEntityData.defineId(KingSlimeEntity.class, EntityDataSerializers.BOOLEAN);
    public boolean isTeleporting() { return entityData.get(IsTeleporting); }
    public void setTeleporting(boolean flag) { entityData.set(IsTeleporting, flag); }
    public static final EntityDataAccessor<Boolean> TeleportEmerging = SynchedEntityData.defineId(KingSlimeEntity.class, EntityDataSerializers.BOOLEAN);
    public boolean isEmerging() { return entityData.get(TeleportEmerging); }
    public void setEmerging(boolean flag) { entityData.set(TeleportEmerging, flag); }

    public static final EntityDataAccessor<Integer> PriorCameraType = SynchedEntityData.defineId(KingSlimeEntity.class, EntityDataSerializers.INT);
    public int getOldCameraType(){return entityData.get(PriorCameraType);}
    public void setOldCameraType(int type) {
        entityData.set(PriorCameraType, type);
    }
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Size", getSize());
        tag.putInt("LargestSize", entityData.get(LargestSize));
        tag.putInt("TeleportTimer", getTeleportTimer());
        tag.putBoolean("TeleportEmerging", isEmerging());
        tag.putBoolean("Teleporting", isTeleporting());
        if (teleportPosition != null){
            tag.putDouble("teleportPosX", teleportPosition.x);
            tag.putDouble("teleportPosY", teleportPosition.y);
            tag.putDouble("teleportPosZ", teleportPosition.z);
        }
        tag.putBoolean("Fleeing", isFleeing());
        tag.putBoolean("FromSlimeRain", isFromSlimeRain());
        tag.putInt("CameraType", getOldCameraType());
        //tag.putInt("AIState", getState());
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSize(tag.getInt("Size"));
        forceSetLargestSize(tag.getInt("LargestSize"));
        setTeleportTimer(tag.getInt("TeleportTimer"));
        setTeleporting(tag.getBoolean("Teleporting"));
        setEmerging(tag.getBoolean("TeleportEmerging"));
        if (tag.contains("teleportPosX")){
            teleportPosition = new Vec3(
                    tag.getDouble("teleportPosX"),
                    tag.getDouble("teleportPosY"),
                    tag.getDouble("teleportPosZ"));
        }
        setFleeing(tag.getBoolean("Fleeing"));
        setFromSlimeRain(tag.getBoolean("FromSlimeRain"));
        setOldCameraType(tag.getInt("CameraType"));

        bossEvent.MaxProgress = getLargestSize();
        //setState(tag.getInt("AIState"));
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(Size, 1);
        entityData.define(LargestSize, 1);
        entityData.define(TeleportTimer, -1);
        entityData.define(IsTeleporting, false);
        entityData.define(TeleportEmerging, false);
        entityData.define(Fleeing, false);
        entityData.define(FromSlimeRain, false);
        entityData.define(PriorCameraType, -1);
        //entityData.define(AIState, 0);
    }
    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> pKey) {
        if (Size.equals(pKey)) {
            this.refreshDimensions();
            this.setYRot(this.yHeadRot);
            this.yBodyRot = this.yHeadRot;
            if (this.isInWater() && this.random.nextInt(20) == 0) {
                this.doWaterSplashEffect();
            }
        }
        bossEvent.MaxProgress = getLargestSize();

        super.onSyncedDataUpdated(pKey);
    }

    public static AttributeSupplier.Builder createAttributes(){
        return KingSlimeEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1D)
                .add(Attributes.ARMOR, 1)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.JUMP_STRENGTH, 0.5D)
                .add(Attributes.ATTACK_DAMAGE, 10D)
                .add(Attributes.ATTACK_KNOCKBACK, 0D)
                .add(Attributes.ATTACK_SPEED, 2D);
    }

    private final int ignoreTargetThreshold = 20;
    public static final int fleeSize = 20;
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new KingSlimeFloatGoal(this));
        this.goalSelector.addGoal(2, new KingSlimeAttackGoal(this));
        this.goalSelector.addGoal(3, new KingSlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new KingSlimeKeepOnJumpingGoal(this));

        this.goalSelector.addGoal(1, new KingSlimeFleeGoal<>(this, Player.class, fleeSize, 16, 30,
                (p) -> !(p.isCreative() || p.isSpectator())));

        this.goalSelector.addGoal(2, new KingSlimeSpawnMinionsGoal(this, 400, 200));
        this.goalSelector.addGoal(2, new KingSlimeTeleportGoal(this, 100));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Slime.class, 10, true, false,
                (target) -> getSize() <= getLargestSize() && target.canCollideWith(this)));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Slime.class, 10, true, false,
                (target) -> getSize() <= getLargestSize() && getSize() <= ignoreTargetThreshold && target.canCollideWith(this)));
    }

    @Override
    public void tick() {
        super.tick();

        if (!isAlive()) return;

        int i = this.getSize();
        if (this.onGround() && !this.wasOnGround) {
            for(int j = 0; j < i; ++j) {
                float f = this.random.nextFloat() * ((float)Math.PI * 2F);
                float f1 = this.random.nextFloat() * 0.5F + 0.5F;
                float f2 = Mth.sin(f) * (float)i * 0.025F * f1;
                float f3 = Mth.cos(f) * (float)i * 0.025F * f1;
                this.level().addParticle(ParticleTypes.ITEM_SLIME,
                        this.getX() + (double)f2, this.getY(), this.getZ() + (double)f3, 0.0D, 0.0D, 0.0D);
            }

            this.playSound(i > 10 ? SoundEvents.SLIME_SQUISH : SoundEvents.SLIME_SQUISH_SMALL, this.getSoundVolume(),
                    ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
        }

        wasOnGround = onGround();

        if (isFleeing()){
            if (sweat-- <= 0){
                int lowerBound = (int)(i * 0.5);
                int upperBound = (i * 2) + 1;
                if (lowerBound >= 0 || upperBound >= 1){
                    for(int j = 0; j < random.nextInt(lowerBound, upperBound); ++j){
                        float f = this.random.nextFloat() * ((float)Math.PI * 2F);
                        float f1 = this.random.nextFloat() * 0.5F + 0.5F;
                        float f2 = Mth.sin(f) * (float)i * 0.025F * f1;
                        float f3 = Mth.cos(f) * (float)i * 0.025F * f1;
                        this.level().addParticle(ParticleTypes.FALLING_WATER,
                                this.getX() + (double)f2,
                                this.getY() + getBbHeight() + (random.nextDouble() / 2) * (random.nextBoolean() ? 1 : -1),
                                this.getZ() + (double)f3,
                                0.0D, 0.4D, 0.0D);
                    }
                    sweat = random.nextInt(10) + 10;
                }
            }
        }

        if (tickCount % 20 == 0){
            boolean flag = false;
            double gainedSize = 0;
            for (ItemEntity ie : level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(1))){
                if (ie.getItem().is(Items.SLIME_BALL)){
                    gainedSize += ie.getItem().getCount() * 0.5;
                    ie.discard();
                    if (!flag) flag = true;
                }
            }
            sizeUpBy((int)gainedSize);
            if (flag) playSound(SoundEvents.ITEM_PICKUP, 1f, 2f);
        }

        if (TickTeleport()){
            if (teleportPosition != null && !isEmerging()){
                teleportTo(teleportPosition.x, teleportPosition.y, teleportPosition.z);
                teleportPosition = null;
                setTeleportTimer(40);
                setEmerging(true);
            } else {
                setTeleportTimer(-1);
                setEmerging(false);
            }
        } else if (getTarget() != null && !isEmerging()
                && getTeleportTimer() != -1 && getTeleportTimer() % 20 == 0
                && random.nextBoolean()){
            teleportPosition = getTarget().position();
        }
    }

    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return getSize() < 10 ? SoundEvents.SLIME_HURT_SMALL : SoundEvents.SLIME_HURT;
    }

    protected SoundEvent getDeathSound() {
        return getSize() < 10 ? SoundEvents.SLIME_DEATH_SMALL : SoundEvents.SLIME_DEATH;
    }

    @Override
    public void setTarget(@Nullable LivingEntity pTarget) {
        super.setTarget(pTarget);
        if (pTarget instanceof ServerPlayer player) bossEvent.addPlayer(player);
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float amount) {
        if (pSource.is(DamageTypes.IN_WALL) || isEmerging()) return false;

        if (pSource.getEntity() != null && pSource.getEntity().isPassenger()) amount /= 4;
        boolean flag = super.hurt(pSource, amount);
        if (flag){
            sizeDownBy((int)Math.min(amount, getSize()));

            if (level() instanceof ServerLevel server){
                int i = this.getSize();
                server.sendParticles(ParticleTypes.ITEM_SLIME,
                        this.getX(),
                        this.getY() + (getBbHeight() / 2),
                        this.getZ(),
                        i, getBbWidth() * 0.5, getBbHeight() * 0.5, getBbWidth() * 0.5, 0.1d);
            }

            if (random.nextBoolean()){
                for (Slime s : level().getEntitiesOfClass(Slime.class, getBoundingBox().inflate(16), (s) -> s.canCollideWith(this))){
                    if (random.nextBoolean()) continue;
                    s.setTarget(this);
                }
            }

            if (pSource.getEntity() instanceof ServerPlayer p) bossEvent.addPlayer(p);
        }
        return flag;
    }
    @Override
    public void push(@NotNull Entity entity) {
        if (isEmerging()) return;

        super.push(entity);
        if (this.isAlive() && entity instanceof LivingEntity LE && this.canAttack(LE)) {
            if (LE.isPassenger()){
                if (LE.hurt(this.damageSources().mobAttack(this), (float)getAttributeValue(Attributes.ATTACK_DAMAGE) / 2)){
                    this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                    this.doEnchantDamageEffects(this, LE);
                }

                Minecraft minecraft = Minecraft.getInstance();
                if (LE instanceof Player p && p.getGameProfile() == minecraft.player.getGameProfile()){
                    if (getOldCameraType() == -1){
                        setOldCameraType(minecraft.options.getCameraType().ordinal());
                    }
                    minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                }
            } else {
                int i = this.getSize();
                if (LE instanceof Slime slime){
                    if (!slime.canCollideWith(this)) return;
                    int gainedSize = slime.getSize();
                    slime.discard();
                    sizeUpBy(gainedSize * 4);
                    playSound(SoundEvents.ITEM_PICKUP, 1f, 0.5f);
                } else if (!isFleeing() && /*this.distanceToSqr(LE) < 0.6D * (double) i * 0.6D * (double) i &&*/ this.hasLineOfSight(LE)) {
                    if (this.distanceToSqr(LE.getEyePosition()) < this.distanceToSqr(LE.position())
                            && this.getBbWidth() > LE.getBbWidth() && this.getBbHeight() > LE.getBbHeight()){
                        LE.startRiding(this, true);
                        if (LE == getTarget()) {
                            setTeleportTimer(-1);
                            teleportPosition = null;
                        }
                    }
                    else if (LE.hurt(this.damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE))){
                        this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                        this.doEnchantDamageEffects(this, LE);
                    }
                }
            }
        }
    }
    @Override
    public boolean canAttack(@NotNull LivingEntity pTarget) {
        return !(pTarget instanceof KingSlimeEntity) && super.canAttack(pTarget);
    }

    @Override
    public float getScale() {
        return getSizeScale();
    }

    @Override
    public void die(@NotNull DamageSource pDamageSource) {
        super.die(pDamageSource);
        if (isFromSlimeRain() && JarGeneralSaveData.isSlimeRain() && level() instanceof ServerLevel s) {
            SlimeRainManager.StopSlimeRain(s, false, false, true);
        }
        bossEvent.removeAllPlayers();
        bossEvent.setVisible(false);
    }
    /*@Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return super.getDimensions(pose).scale(1f + (getSize() * 0.01f));
    }*/

    @Override
    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, @NotNull DamageSource pSource) {
        return false;
    }

    protected int getJumpDelay() {
        return isFleeing() ? 0 : 10;
    }

    @Override
    protected float getSoundVolume() {
        return (float)Math.min(0.1 * getSize(), 3);
    }
    protected float getSoundPitch() {
        return (float)Math.max(3 - (0.05 * getSize()), 0.5);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0;
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }
    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "KingSlimeManager", event ->{
            if (getTeleportTimer() != -1){
                return event.setAndContinue(RawAnimation.begin()
                        .then(isEmerging() ? "teleport_end" : "teleport", Animation.LoopType.PLAY_ONCE));
            } else return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static class KingSlimeAttackGoal extends Goal {
        private final KingSlimeEntity kSlime;
        private int growTiredTimer;

        public KingSlimeAttackGoal(KingSlimeEntity slime) {
            this.kSlime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }
        public boolean canUse() {
            LivingEntity livingentity = this.kSlime.getTarget();
            if (livingentity == null) {
                return false;
            } else {
                if (kSlime.getSize() <= fleeSize){
                    return livingentity instanceof Slime;
                }
                return this.kSlime.canAttack(livingentity) && this.kSlime.getMoveControl() instanceof KingSlimeMoveControl;
            }
        }
        public void start() {
            this.growTiredTimer = reducedTickDelay(300);
            super.start();
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = this.kSlime.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!this.kSlime.canAttack(livingentity)) {
                return false;
            } else {
                if (kSlime.getSize() <= fleeSize){
                    return livingentity instanceof Slime;
                }
                return --this.growTiredTimer > 0;
            }
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            LivingEntity livingentity = this.kSlime.getTarget();
            if (livingentity != null) {
                this.kSlime.lookAt(livingentity, 10.0F, 10.0F);
            }

            MoveControl movecontrol = this.kSlime.getMoveControl();
            if (movecontrol instanceof KingSlimeMoveControl sControl) {
                sControl.setDirection(this.kSlime.getYRot(), this.kSlime.isAggressive());
            }

        }
    }
    public static class KingSlimeFloatGoal extends Goal {
        private final KingSlimeEntity kSlime;

        public KingSlimeFloatGoal(KingSlimeEntity pSlime) {
            this.kSlime = pSlime;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
            pSlime.getNavigation().setCanFloat(true);
        }

        public boolean canUse() {
            return (this.kSlime.isInWater() || this.kSlime.isInLava()) && this.kSlime.getMoveControl() instanceof KingSlimeMoveControl;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            if (this.kSlime.getRandom().nextFloat() < 0.8F) {
                this.kSlime.getJumpControl().jump();
            }

            MoveControl movecontrol = this.kSlime.getMoveControl();
            if (movecontrol instanceof KingSlimeMoveControl sControl) {
                sControl.setWantedMovement(1.2D);
            }

        }
    }
    public static class KingSlimeKeepOnJumpingGoal extends Goal {
        private final KingSlimeEntity slime;

        public KingSlimeKeepOnJumpingGoal(KingSlimeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return !this.slime.isPassenger() && slime.getTeleportTimer() <= -1;
        }

        public void tick() {
            MoveControl movecontrol = this.slime.getMoveControl();
            if (movecontrol instanceof KingSlimeMoveControl sControl) {
                sControl.setWantedMovement(1.0D);
            }
        }
    }
    public static class KingSlimeMoveControl extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final KingSlimeEntity kSlime;
        private boolean isAggressive;
        private int jumpCount;
        private final int largeJumpThreshold;

        public KingSlimeMoveControl(KingSlimeEntity kSlime, int largeJumpThreshold) {
            super(kSlime);
            this.kSlime = kSlime;
            this.largeJumpThreshold = largeJumpThreshold;
            this.yRot = 180.0F * kSlime.getYRot() / (float)Math.PI;
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
            if (kSlime.getTeleportTimer() > 0) {
                this.operation = Operation.WAIT;
                //return;
            }

            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0F));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setZza(0.0F);
                this.mob.setYya(0.0F);
            } else {
                this.operation = MoveControl.Operation.WAIT;
                if (this.mob.onGround()) {
                    this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                    if (this.jumpDelay-- <= 0) {
                        jumpCount = jumpCount++ < largeJumpThreshold ? jumpCount : 0;
                        this.jumpDelay = this.kSlime.getJumpDelay() * (jumpCount == largeJumpThreshold ? 2 : 1);
                        if (this.isAggressive) {
                            this.jumpDelay /= 2;
                        }

                        this.kSlime.getJumpControl().jump();
                        if (jumpCount == 0 && !kSlime.isFleeing()){
                            this.kSlime.yya += 0.75d * kSlime.getAttributeValue(Attributes.JUMP_STRENGTH);
                            this.kSlime.xxa *= 3f;
                            this.kSlime.zza *= 3f;
                        }
                        else{
                            if (kSlime.isFleeing()){
                                this.kSlime.xxa *= 4f;
                                this.kSlime.zza *= 4f;
                                this.kSlime.yya += 0.1d * kSlime.getAttributeValue(Attributes.JUMP_STRENGTH) * kSlime.random.nextDouble();
                            }
                            this.kSlime.yya += 0.1d * kSlime.getAttributeValue(Attributes.JUMP_STRENGTH);
                        }
                        this.kSlime.playSound(SoundEvents.SLIME_JUMP, this.kSlime.getSoundVolume(), this.kSlime.getSoundPitch());
                    } else {
                        this.kSlime.xxa = 0.0F;
                        this.kSlime.zza = 0.0F;
                        this.kSlime.yya = 0.0f;
                        this.mob.setSpeed(0.0F);
                    }
                } else {
                    this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                }

            }
        }
    }
    public static class KingSlimeRandomDirectionGoal extends Goal {
        private final KingSlimeEntity kSlime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public KingSlimeRandomDirectionGoal(KingSlimeEntity slime) {
            this.kSlime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {
            return this.kSlime.getTarget() == null &&
                    (this.kSlime.onGround() || this.kSlime.isInWater() || this.kSlime.isInLava()
                            || this.kSlime.hasEffect(MobEffects.LEVITATION)) && this.kSlime.getMoveControl() instanceof KingSlimeMoveControl;
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = this.adjustedTickDelay(40 + this.kSlime.getRandom().nextInt(60));
                this.chosenDegrees = (float)this.kSlime.getRandom().nextInt(360);
            }

            MoveControl movecontrol = this.kSlime.getMoveControl();
            if (movecontrol instanceof KingSlimeMoveControl sControl) {
                sControl.setDirection(this.chosenDegrees, false);
            }

        }
    }
    public static class KingSlimeFleeGoal<T extends LivingEntity> extends Goal{
        private final KingSlimeEntity kSlime;
        private final Class<T> toFleeFrom;
        private final int fleeSize;
        private final int wantedDistance;
        private final int repathInterval;
        private @Nullable final Predicate<T> FleePredicate;
        public KingSlimeFleeGoal(KingSlimeEntity parent, Class<T> toFleeFrom, int fleeSize, int wantedDistance,
                                 int repathInterval, @Nullable Predicate<T> fleePredicate){
            this.kSlime = parent;
            this.toFleeFrom = toFleeFrom;
            this.fleeSize = fleeSize;
            this.wantedDistance = wantedDistance;
            this.repathInterval = repathInterval;
            this.FleePredicate = fleePredicate;
        }
        private int size(){
            return kSlime.getSize();
        }
        @Override
        public boolean canUse() {
            return size() <= fleeSize;
        }

        @Override
        public boolean canContinueToUse() {
            return size() < fleeSize + 10;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            kSlime.setFleeing(true);
            kSlime.setTeleporting(false);
            kSlime.setEmerging(false);
            kSlime.setTeleportTimer(-1);
            kSlime.teleportPosition = null;
        }

        @Override
        public void stop() {
            kSlime.setFleeing(false);
        }

        private static final int stopToEatRange = 6;
        @Override
        public void tick() {
            if (kSlime.tickCount % repathInterval == 0){
                Slime closestSlime = null;
                double SlimeDistance = wantedDistance;
                for (Slime s : kSlime.level().getEntitiesOfClass(Slime.class, kSlime.getBoundingBox().inflate(stopToEatRange))){
                    double distance1 = s.distanceTo(kSlime);
                    if (distance1 < SlimeDistance){
                        closestSlime = s;
                        SlimeDistance = distance1;
                    }
                }
                if (closestSlime != null){
                    kSlime.setTarget(closestSlime);
                    return;
                }

                T closest = null;
                double distance = wantedDistance;
                for (T t : locateNearbyHostiles()){
                    double distance1 = t.distanceTo(kSlime);
                    if (distance1 < distance){
                        closest = t;
                        distance = distance1;
                    }
                }
                CheckAndFlee(closest);
            }
        }

        private void CheckAndFlee(T target) {
            if (target == null){
                stop();
                return;
            }

            Vec3 away;
            int cycle = 0;
            do{
                away = DefaultRandomPos.getPosAway(kSlime, wantedDistance, 1, target.position());
            } while (away == null && cycle++ < 10);

            if (away != null){
                this.kSlime.lookAt(EntityAnchorArgument.Anchor.EYES, away);

                MoveControl movecontrol = this.kSlime.getMoveControl();
                if (movecontrol instanceof KingSlimeMoveControl sControl) {
                    sControl.setDirection(this.kSlime.getYRot(), this.kSlime.isAggressive());
                }

                kSlime.setFleeing(true);
            }
        }

        private List<T> locateNearbyHostiles(){
            return kSlime.level().getEntitiesOfClass(toFleeFrom, kSlime.getBoundingBox().inflate(kSlime.getAttributeValue(Attributes.FOLLOW_RANGE)),
                    (t) -> FleePredicate == null || FleePredicate.test(t));
        }
    }
    public static class KingSlimeSpawnMinionsGoal extends Goal{
        private final KingSlimeEntity kSlime;
        private final IntegerCycleTracker.Randomized spawnTimer;
        public KingSlimeSpawnMinionsGoal(KingSlimeEntity kSlime, int baseTimer, int timerClamp){
            this.kSlime = kSlime;
            this.spawnTimer = new IntegerCycleTracker.Randomized(baseTimer, timerClamp);
        }
        @Override
        public boolean canUse() {
            return kSlime.getTarget() != null && !(kSlime.getTarget() instanceof Slime) && kSlime.getSize() > 30 && !kSlime.level().isClientSide();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (spawnTimer.tick()){
                final int count = kSlime.random.nextInt(Math.max(kSlime.getSize() / 30, 1), Math.max(kSlime.getSize() / 20, 2));
                for (int i = 0; i < count; i++){
                    Vec3 direction = kSlime.getForward()
                            .xRot((float)Math.toDegrees(-45f))
                            .yRot((float)Math.toDegrees(kSlime.random.nextInt(-180, 181)));
                    Vec3 spawnPos = kSlime.position().add(0, kSlime.getBbHeight(), 0).add(direction);
                    spawnSlime(spawnPos, direction);
                }
            }
        }
        private void spawnSlime(Vec3 spawnPos, Vec3 direction) {
            Slime slime = new Slime(EntityType.SLIME, kSlime.level()){
                @Override
                public void tick() {
                    super.tick();
                    if (this.tickCount > 300 * getSize()) discard();
                }
                @Override
                protected boolean shouldDropLoot() {
                    return false;
                }
                @Override
                public void push(@NotNull Entity pEntity) {
                    if (pEntity instanceof KingSlimeEntity) return;
                    super.push(pEntity);
                }
                @Override
                public boolean canCollideWith(@NotNull Entity pEntity) {
                    return super.canCollideWith(pEntity) && !(pEntity instanceof KingSlimeEntity);
                }
                @Override
                public boolean causeFallDamage(float pFallDistance, float pMultiplier, @NotNull DamageSource pSource) {
                    return false;
                }
                @Override
                protected boolean isDealsDamage() {
                    return true;
                }
                @Override
                protected float getAttackDamage() {
                    return super.getAttackDamage() + 2;
                }
            };
            slime.setCustomName(Component.literal("§2King Slime's Minion"));

            AttributeInstance slimeSpeed = slime.getAttribute(Attributes.MOVEMENT_SPEED);
            if (slimeSpeed != null){
                slimeSpeed.setBaseValue(slimeSpeed.getBaseValue() * 1.5);
            }

            slime.setSize(kSlime.random.nextInt(2,5), true);
            slime.setPos(spawnPos);
            slime.setDeltaMovement(direction.scale(kSlime.getSizeScale() * 0.1));
            slime.setTarget(kSlime.getTarget());
            kSlime.level().addFreshEntity(slime);
        }
    }
    public static class KingSlimeTeleportGoal extends Goal{
        public final KingSlimeEntity kSlime;
        public final int cooldown;
        public int activeCD;
        public KingSlimeTeleportGoal(KingSlimeEntity kSlime, int cd){
            this.kSlime = kSlime;
            this.cooldown = cd;
            activeCD = cd;
        }
        @Override
        public boolean canUse() {
            return kSlime.getTarget() != null && !kSlime.isFleeing() && !kSlime.hasPassenger((t) -> true) && kSlime.getTeleportTimer() == -1 && --activeCD <= 0;
        }
        @Override
        public void start() {
            LivingEntity target = kSlime.getTarget();
            if (target != null) {
                kSlime.teleportPosition = target.position();
                kSlime.setTeleportTimer(30);
                kSlime.setEmerging(false);
            }
            activeCD = cooldown;
        }

        @Override
        public void stop() {
            kSlime.setTeleportTimer(-1);
            kSlime.setEmerging(false);
        }
    }

    public static class BossEvent extends ServerBossEvent{
        public int MaxProgress;
        public BossEvent(Component pName, int maxProgress) {
            super(pName, BossBarColor.GREEN, BossBarOverlay.PROGRESS);
            this.MaxProgress = maxProgress;
        }
        public void generateAndSetProgress(int reference){
            setProgress(Math.min((float) reference / MaxProgress, 1.0f));
            System.out.println("Progress is now " + getProgress());
        }
    }
}
