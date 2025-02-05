package mod.pilot.jar_of_chaos.entities.projectiles;

import mod.pilot.jar_of_chaos.effects.JarEffects;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.items.JarItems;
import mod.pilot.jar_of_chaos.sound.JarSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlimeArrowProjectile extends AbstractArrow {
    public SlimeArrowProjectile(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static final EntityDataAccessor<Integer> MaxBounces = SynchedEntityData.defineId(SlimeArrowProjectile.class, EntityDataSerializers.INT);
    public int getMaxBounces(){return entityData.get(MaxBounces);}
    public void setMaxBounces(int count) {entityData.set(MaxBounces, count);}
    public static final EntityDataAccessor<Integer> Bounces = SynchedEntityData.defineId(SlimeArrowProjectile.class, EntityDataSerializers.INT);
    public int getBounces(){return entityData.get(Bounces);}
    public void setBounces(int count) {entityData.set(Bounces, count);}
    public void addBounce(){setBounces(getBounces() + 1);}

    public static final EntityDataAccessor<Integer> TimeSinceLastBounce = SynchedEntityData.defineId(SlimeArrowProjectile.class, EntityDataSerializers.INT);
    public int getBounceTime(){return entityData.get(TimeSinceLastBounce);}
    public void setBounceTime(int count) {entityData.set(TimeSinceLastBounce, count);}
    public void addBounceTime(){setBounceTime(getBounceTime() + 1);}
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("bounces", getBounces());
        tag.putInt("bounceTime", getBounceTime());
        tag.putInt("mBounces", getMaxBounces());
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBounces(tag.getInt("bounces"));
        setBounceTime(tag.getInt("bounceTime"));
        setMaxBounces(tag.getInt("mBounces"));
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MaxBounces, 1);
        entityData.define(TimeSinceLastBounce, 0);
        entityData.define(Bounces, 0);
    }

    private static final int defaultMaxBounce = 50;
    public static SlimeArrowProjectile create(double pX, double pY, double pZ, Level pLevel){
        return create(pX, pY, pZ, pLevel, defaultMaxBounce);
    }
    public static SlimeArrowProjectile create(double pX, double pY, double pZ, Level pLevel, int maxBounces){
        SlimeArrowProjectile arrow = new SlimeArrowProjectile(pX, pY, pZ, pLevel);
        arrow.setMaxBounces(maxBounces);
        return arrow;
    }
    public static SlimeArrowProjectile create(LivingEntity pShooter, Level pLevel){
        return create(pShooter, pLevel, defaultMaxBounce);
    }
    public static SlimeArrowProjectile create(LivingEntity pShooter, Level pLevel, int maxBounces){
        SlimeArrowProjectile arrow = new SlimeArrowProjectile(pShooter, pLevel);
        arrow.setMaxBounces(maxBounces);
        return arrow;
    }
    protected SlimeArrowProjectile(double pX, double pY, double pZ, Level pLevel) {
        super(JarEntities.SLIME_ARROW.get(), pX, pY, pZ, pLevel);
    }
    protected SlimeArrowProjectile(LivingEntity pShooter, Level pLevel) {
        super(JarEntities.SLIME_ARROW.get(), pShooter, pLevel);
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(JarItems.SLIME_ARROW.get());
    }

    @Nullable
    public BlockState lastState;

    //Ported over from AbstractArrow for managing/overriding of private variables and methods
    @Override
    public void tick() {
        boolean flag = this.isNoPhysics();
        Vec3 vec3 = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            double d0 = vec3.horizontalDistance();
            this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
            this.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level().getBlockState(blockpos);
        /*if (!blockstate.isAir() && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
            if (!voxelshape.isEmpty()) {
                Vec3 vec31 = this.position();

                for(AABB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(blockpos).contains(vec31)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }*/

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW) || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType))) {
            this.clearFire();
        }

        if (this.inGround && !flag) {
            if (this.lastState != blockstate
                    && this.level().noCollision((new AABB(this.position(), this.position())).inflate(0.06D))) {
                this.inGround = false;
                Vec3 delta = this.getDeltaMovement();
                this.setDeltaMovement(delta.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F)));
            } else if (!this.level().isClientSide) {
                this.tickDespawn();
            }

            ++this.inGroundTime;
        } else {
            this.inGroundTime = 0;
            Vec3 vec32 = this.position();
            Vec3 vec33 = vec32.add(vec3);
            HitResult hitresult = this.level().clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (hitresult.getType() != HitResult.Type.MISS) {
                vec33 = hitresult.getLocation();
            }

            while(!this.isRemoved()) {
                EntityHitResult entityhitresult = this.findHitEntity(vec32, vec33);
                if (entityhitresult != null) {
                    hitresult = entityhitresult;
                }

                if (hitresult != null && hitresult.getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult)hitresult).getEntity();
                    Entity entity1 = this.getOwner();
                    if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                        hitresult = null;
                        entityhitresult = null;
                    }
                }

                if (hitresult != null && hitresult.getType() != HitResult.Type.MISS && !flag) {
                    switch (net.minecraftforge.event.ForgeEventFactory.onProjectileImpactResult(this, hitresult)) {
                        case SKIP_ENTITY:
                            if (hitresult.getType() != HitResult.Type.ENTITY) { // If there is no entity, we just return default behaviour
                                this.onHit(hitresult);
                                this.hasImpulse = true;
                                break;
                            }
                            //ignoredEntities.add(entityhitresult.getEntity().getId());
                            entityhitresult = null; // Don't process any further
                            break;
                        case STOP_AT_CURRENT_NO_DAMAGE:
                            this.discard();
                            entityhitresult = null; // Don't process any further
                            break;
                        case STOP_AT_CURRENT:
                            this.setPierceLevel((byte) 0);
                        case DEFAULT:
                            this.onHit(hitresult);
                            this.hasImpulse = true;
                            break;
                    }
                }

                if (entityhitresult == null || this.getPierceLevel() <= 0) {
                    break;
                }

                hitresult = null;
            }

            if (this.isRemoved())
                return;

            vec3 = this.getDeltaMovement();
            double d5 = vec3.x;
            double d6 = vec3.y;
            double d1 = vec3.z;
            if (this.isCritArrow()) {
                for(int i = 0; i < 4; ++i) {
                    this.level().addParticle(ParticleTypes.ITEM_SLIME, this.getX() + d5 * (double)i / 4.0D, this.getY() + d6 * (double)i / 4.0D, this.getZ() + d1 * (double)i / 4.0D, -d5, -d6 + 0.2D, -d1);
                }
            }

            double d7 = this.getX() + d5;
            double d2 = this.getY() + d6;
            double d3 = this.getZ() + d1;
            double d4 = vec3.horizontalDistance();
            if (flag) {
                this.setYRot((float)(Mth.atan2(-d5, -d1) * (double)(180F / (float)Math.PI)));
            } else {
                this.setYRot((float)(Mth.atan2(d5, d1) * (double)(180F / (float)Math.PI)));
            }

            this.setXRot((float)(Mth.atan2(d6, d4) * (double)(180F / (float)Math.PI)));
            this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
            this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
            float f = 0.99F;
            float f1 = 0.05F;
            if (this.isInWater()) {
                for(int j = 0; j < 4; ++j) {
                    float f2 = 0.25F;
                    this.level().addParticle(ParticleTypes.BUBBLE, d7 - d5 * 0.25D, d2 - d6 * 0.25D, d3 - d1 * 0.25D, d5, d6, d1);
                }

                f = this.getWaterInertia();
            }

            this.setDeltaMovement(vec3.scale((double)f));
            if (!this.isNoGravity() && !flag) {
                Vec3 vec34 = this.getDeltaMovement();
                this.setDeltaMovement(vec34.x, vec34.y - (double)0.05F, vec34.z);
            }

            this.setPos(d7, d2, d3);
            this.checkInsideBlocks();
        }


    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        SpawnParticles();
        if (getMaxBounces() < getBounces()) this.discard();
        this.lastState = level().getBlockState(result.getBlockPos());

        addBounce();
        Vec3 scale = new Vec3(result.getDirection().step().absolute().negate()).scale(2).add(1, 1, 1).scale(1.1f);
        this.setDeltaMovement(getDeltaMovement().multiply(scale));
        playSound(JarSounds.BOING.get());
        setBounceTime(0);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity target = result.getEntity();
        target.setDeltaMovement(getDeltaMovement());
        if (target instanceof LivingEntity le){
            le.addEffect(new MobEffectInstance(JarEffects.SPLAT.get(), 100));
        }
        if (target.verticalCollisionBelow) target.addDeltaMovement(new Vec3(0, 0.5, 0));
        this.discard();
    }

    private void SpawnParticles(){
        if (level() instanceof ServerLevel server){
            Vec3 pos = position();
            server.sendParticles(ParticleTypes.ITEM_SLIME, pos.x, pos.y, pos.z, 5, 0.1, 0.1, 0.1, 0.1);
        }
    }
}
