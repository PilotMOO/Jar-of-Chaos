package mod.pilot.jar_of_chaos.entities.projectiles;

import mod.pilot.jar_of_chaos.entities.JarEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SlimeBallProjectile extends Projectile implements ItemSupplier {
    public SlimeBallProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    public static SlimeBallProjectile createAt(Level level, Vec3 position, @Nullable Vec3 startingDelta, int maxBounces){
        SlimeBallProjectile sBall = new SlimeBallProjectile(level, maxBounces);
        sBall.moveTo(position);
        sBall.setDeltaMovement(startingDelta != null ? startingDelta : Vec3.ZERO);
        level.addFreshEntity(sBall);
        return sBall;
    }
    private SlimeBallProjectile(Level level, int maxBounces){
        super(JarEntities.SLIME_BALL.get(), level);
        setMaxBounces(maxBounces);
    }

    public static final EntityDataAccessor<Integer> MaxBounces = SynchedEntityData.defineId(SlimeBallProjectile.class, EntityDataSerializers.INT);
    public int getMaxBounces(){return entityData.get(MaxBounces);}
    public void setMaxBounces(int count) {entityData.set(MaxBounces, count);}
    public static final EntityDataAccessor<Integer> Bounces = SynchedEntityData.defineId(SlimeBallProjectile.class, EntityDataSerializers.INT);
    public int getBounces(){return entityData.get(Bounces);}
    public void setBounces(int count) {entityData.set(Bounces, count);}
    public void addBounce(){setBounces(getBounces() + 1);}
    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("bounces", getBounces());
        tag.putInt("mBounces", getMaxBounces());
    }
    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBounces(tag.getInt("bounces"));
        setMaxBounces(tag.getInt("mBounces"));
    }
    @Override
    protected void defineSynchedData() {
        entityData.define(MaxBounces, 1);
        entityData.define(Bounces, 0);
    }

    private static final Vec3 gravity = new Vec3(0, -0.02f, 0);
    @Override
    public void tick() {
        super.tick();
        setPos(position().add(getDeltaMovement()));
        if (!this.isNoGravity()) addDeltaMovement(gravity);
        HitChecker();
    }

    protected void HitChecker() {
        //Stolen from AbstractArrow
        Vec3 vec3 = getDeltaMovement();
        Vec3 vec32 = this.position();
        Vec3 vec33 = vec32.add(vec3);
        HitResult hitresult = this.level().clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hitresult.getType() != HitResult.Type.MISS) {
            vec33 = hitresult.getLocation();
        }

        EntityHitResult entityhitresult = this.findHitEntity(vec32, vec33);
        if (entityhitresult != null) {
            hitresult = entityhitresult;
        }

        if (hitresult.getType() == HitResult.Type.ENTITY) {
            assert hitresult instanceof EntityHitResult;
            Entity entity = ((EntityHitResult) hitresult).getEntity();
            Entity entity1 = this.getOwner();
            if (entity instanceof Player && entity1 instanceof Player && !((Player) entity1).canHarmPlayer((Player) entity)) {
                hitresult = null;
            }
        }

        if (hitresult != null && hitresult.getType() != HitResult.Type.MISS) {
            switch (net.minecraftforge.event.ForgeEventFactory.onProjectileImpactResult(this, hitresult)) {
                case SKIP_ENTITY -> {
                    if (hitresult.getType() != HitResult.Type.ENTITY) {
                        this.onHit(hitresult);
                        this.hasImpulse = true;
                    }
                }
                case STOP_AT_CURRENT_NO_DAMAGE -> this.discard();
                case STOP_AT_CURRENT, DEFAULT -> {
                    this.onHit(hitresult);
                    this.hasImpulse = true;
                }
            }
        }
    }
    @Nullable
    protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
        //Also stolen from AbstractArrow
        return ProjectileUtil.getEntityHitResult(this.level(), this, pStartVec, pEndVec, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        result.getEntity().addDeltaMovement(getDeltaMovement().multiply(1, -1, 1));
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        addBounce();
        SpawnParticles();
        if (getBounces() >= getMaxBounces() || Math.abs(getDeltaMovement().y) < 0.01) this.discard();
        else{
            setDeltaMovement(getDeltaMovement().multiply(1, -0.75, 1));
            playSound(SoundEvents.SLIME_BLOCK_FALL, 0.25f, 2f);
        }
    }
    private void SpawnParticles(){
        if (level() instanceof ServerLevel server){
            Vec3 pos = position();
            server.sendParticles(ParticleTypes.ITEM_SLIME, pos.x, pos.y, pos.z, 5, 0.1, 0.1, 0.1, 0.1);
        }
    }

    @Override
    public @NotNull ItemStack getItem() {
        return new ItemStack(Items.SLIME_BALL);
    }
}
