package mod.pilot.jar_of_chaos.entities.projectiles;

import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.pilot.jar_of_chaos.damagetypes.JarDamageTypes;
import mod.pilot.jar_of_chaos.sound.JarSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class GrandPianoProjectile extends Projectile implements GeoAnimatable {
    public GrandPianoProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public enum state{
        inactive,
        falling,
        crashed,
        finished
    }
    public static final EntityDataAccessor<Integer> State = SynchedEntityData.defineId(GrandPianoProjectile.class, EntityDataSerializers.INT);
    public int getState(){return entityData.get(State);}
    public void setState(Integer count) {entityData.set(State, count);}
    public void setState(state ordinal) {entityData.set(State, ordinal.ordinal());}
    public static final EntityDataAccessor<Integer> CrashAge = SynchedEntityData.defineId(GrandPianoProjectile.class, EntityDataSerializers.INT);
    public int getAge(){return entityData.get(CrashAge);}
    public void setAge(Integer age) {entityData.set(CrashAge, age);}
    public void Age(){
        setAge(getAge() + 1);
    }
    protected static final float Gravity = 0.05f;
    protected static final float MaxFallSpeed = 10f;
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", entityData.get(State));
        tag.putInt("CrashAge", entityData.get(CrashAge));
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(State, tag.getInt("State"));
        entityData.set(CrashAge, tag.getInt("CrashAge"));
    }
    @Override
    protected void defineSynchedData() {
        this.entityData.define(State, 1);
        this.entityData.define(CrashAge, 0);
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
    public double getTick(Object object) {
        return 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (getState() != 0){
            ManageVelocity();
        }
    }
    public void ManageVelocity(){
        setDeltaMovement(getDeltaMovement().add(0, getDeltaMovement().y > -MaxFallSpeed ? -Gravity : 0, 0));
        setPos(position().add(getDeltaMovement()));
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        this.CRASH();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity hitEntity = result.getEntity();
        if (this.canHitEntity(hitEntity)){
            hitEntity.setDeltaMovement(getDeltaMovement());
            hitEntity.setPose(Pose.SWIMMING);
        }
        if (hitEntity.onGround()){
            CRASH();
        }
    }

    public void CRASH(){
        if (getState() != 1) return;
        setState(2);
        playSound(JarSounds.PIANO_CRASH.get());
        AABB AOEaabb = getBoundingBox();
        Level level = level();
        for (LivingEntity LE : level.getEntitiesOfClass(LivingEntity.class, AOEaabb, this::canHitEntity)){
            LE.hurt(JarDamageTypes.piano(LE), (float)(getDeltaMovement().y * 5));
        }
        for (BlockPos pos : BlockPos.betweenClosed((int)AOEaabb.minX, (int)AOEaabb.minY, (int)AOEaabb.minZ, (int)AOEaabb.maxX, (int)AOEaabb.maxY, (int)AOEaabb.maxZ)){
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof AbstractGlassBlock){
                level.removeBlock(pos, false);
                level.levelEvent(2001, pos, Block.getId(level.getBlockState(pos)));
                level.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS);
            }
        }
        SpawnCrashParticles();
    }

    private void SpawnCrashParticles() {
        int x = Mth.floor(this.getX());
        int y = Mth.floor(this.getY());
        int z = Mth.floor(this.getZ());
        Level world = this.level();
        RandomSource rand = this.random;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < rand.nextIntBetweenInclusive(10, 30); ++i) {
            blockpos$mutableblockpos.set(x + Mth.nextInt(rand, (int) (-getBbWidth() / 2), (int) (getBbWidth() / 2)), y + Mth.nextInt(rand, 0, (int)(getBbHeight() / 2)), z + Mth.nextInt(rand, (int) (-getBbWidth() / 2), (int) (getBbWidth() / 2)));
            BlockState blockstate = world.getBlockState(blockpos$mutableblockpos);
            if (!blockstate.isSolidRender(world, blockpos$mutableblockpos)) {
                world.addParticle(ParticleTypes.LARGE_SMOKE, (double) blockpos$mutableblockpos.getX() + rand.nextDouble(),
                        (double) blockpos$mutableblockpos.getY() + rand.nextDouble(),
                        (double) blockpos$mutableblockpos.getZ() + rand.nextDouble(),
                        rand.nextDouble() * (rand.nextBoolean() ? 1 : -1),
                        rand.nextDouble(),
                        rand.nextDouble() * (rand.nextBoolean() ? 1 : -1));
            }
        }
    }
}
