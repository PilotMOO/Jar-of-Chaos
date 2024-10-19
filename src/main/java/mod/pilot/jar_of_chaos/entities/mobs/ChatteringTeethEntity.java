package mod.pilot.jar_of_chaos.entities.mobs;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.pilot.jar_of_chaos.entities.AI.ChatteringTeethMovementGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class ChatteringTeethEntity extends PathfinderMob implements GeoEntity {
    public ChatteringTeethEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public enum state{
        idle,
        chase,
        latched
    }
    public static final EntityDataAccessor<Integer> AIState = SynchedEntityData.defineId(ChatteringTeethEntity.class, EntityDataSerializers.INT);
    public int getAIState(){return entityData.get(AIState);}
    public void setAIState(int count) {entityData.set(AIState, count);}
    public void setAIState(state ordinal) {entityData.set(AIState, ordinal.ordinal());}
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
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("AIState",entityData.get(AIState));
        UUID uuid = getOwnerUUID();
        if (uuid != null) {
            tag.putUUID("Owner", uuid);
        }
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(AIState, tag.getInt("AIState"));
        if (tag.contains("Owner")){
            entityData.set(Owner, Optional.of(tag.getUUID("Owner")));
        }
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AIState, 0);
        this.entityData.define(Owner, Optional.empty());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

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
        this.goalSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, false));
        this.goalSelector.addGoal(1, new ChatteringTeethMovementGoal(this, 0.5));
    }

    public static AttributeSupplier.Builder createAttributes(){
        return ChatteringTeethEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 6D)
                .add(Attributes.ARMOR, 1)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 2D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.25D)
                .add(Attributes.ATTACK_SPEED, 2D);
    }


    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        if (entity == getOwner()) return false;
        boolean flag = super.doHurtTarget(entity);
        if (flag && getAIState() == 2) entity.invulnerableTime = 0;
        return flag;
    }
}
