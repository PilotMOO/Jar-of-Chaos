package mod.pilot.jar_of_chaos.entities.projectiles;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.pilot.jar_of_chaos.Config;
import mod.pilot.jar_of_chaos.damagetypes.JarDamageTypes;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.sound.JarSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class GrandPianoProjectile extends Projectile implements GeoEntity {
    public GrandPianoProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setState(1);
    }

    private static final boolean ShouldThePianoBreakTheFabricOfReality = Config.SERVER.should_pianos_crash_harder.get();

    public enum state{
        inactive,
        falling,
        crashed,
        finished
    }
    public static final EntityDataAccessor<Integer> State = SynchedEntityData.defineId(GrandPianoProjectile.class, EntityDataSerializers.INT);
    public int getState(){return entityData.get(State);}
    public void setState(int count) {entityData.set(State, count);}
    public void setState(state ordinal) {entityData.set(State, ordinal.ordinal());}
    public static final int defaultDamage = 10;
    public static final EntityDataAccessor<Integer> Damage = SynchedEntityData.defineId(GrandPianoProjectile.class, EntityDataSerializers.INT);
    public int getDamage(){return entityData.get(Damage);}
    public void setDamage(int count) {entityData.set(Damage, count);}
    public static final EntityDataAccessor<Integer> CrashAge = SynchedEntityData.defineId(GrandPianoProjectile.class, EntityDataSerializers.INT);
    public int getAge(){return entityData.get(CrashAge);}
    public void setAge(Integer age) {entityData.set(CrashAge, age);}
    public void Age(){
        setAge(getAge() + 1);
    }
    public static final Float defaultGravity = 0.05f;
    public static final EntityDataAccessor<Float> Gravity = SynchedEntityData.defineId(GrandPianoProjectile.class, EntityDataSerializers.FLOAT);
    public float getGravity(){return entityData.get(Gravity);}
    public void setGravity(float count) {entityData.set(Gravity, count);}
    public static final float defaultMaxFallSpeed = 10f;
    public static final EntityDataAccessor<Float> MaxFallSpeed = SynchedEntityData.defineId(GrandPianoProjectile.class, EntityDataSerializers.FLOAT);
    public float getMaxFallSpeed(){return entityData.get(MaxFallSpeed);}
    public void setMaxFallSpeed(float count) {entityData.set(MaxFallSpeed, count);}
    public static final EntityDataAccessor<Float> yRot = SynchedEntityData.defineId(GrandPianoProjectile.class, EntityDataSerializers.FLOAT);
    public float getYRot(){return entityData.get(yRot);}
    public void setYRot(float rot) {entityData.set(yRot, rot);}
    protected static final int MaxAge = 40;
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", entityData.get(State));
        tag.putInt("CrashAge", entityData.get(CrashAge));
        tag.putInt("Damage", entityData.get(Damage));
        tag.putFloat("Gravity", entityData.get(Gravity));
        tag.putFloat("MaxFallSpeed", entityData.get(MaxFallSpeed));
        tag.putFloat("YRot", entityData.get(yRot));
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(State, tag.getInt("State"));
        entityData.set(CrashAge, tag.getInt("CrashAge"));
        entityData.set(Damage, tag.getInt("Damage"));
        entityData.set(Gravity, tag.getFloat("Gravity"));
        entityData.set(MaxFallSpeed, tag.getFloat("MaxFallSpeed"));
        entityData.set(yRot, tag.getFloat("YRot"));
    }
    @Override
    protected void defineSynchedData() {
        this.entityData.define(State, 1);
        this.entityData.define(CrashAge, 0);
        this.entityData.define(Damage, defaultDamage);
        this.entityData.define(Gravity, defaultGravity);
        this.entityData.define(MaxFallSpeed, defaultMaxFallSpeed);
        this.entityData.define(yRot, 0f);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "CrashManager", event -> {
            if (getState() == 2){
                event.setAndContinue(RawAnimation.begin().thenPlayAndHold("Crash"));
            }
            return PlayState.CONTINUE;
        }));
    }
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /**
     * Method to spawn a piano above a position or target.
     *
     * @param parent The "Owner" of the piano, Nullable. The Owner cannot be hurt by the piano
     * @param startingPos The starting position of the piano, basically the desired landing position.
     *                    You may feed in null here IF the parent is not null
     * @param level The level for the entity to be created in, cannot be null for obvious reasons
     * @param spawnHeight How high above the spawn position will the piano spawn
     * @param gravity How fast the piano will pick up speed when falling. Nullable, if null it will default to 0.05f
     * @param maxFallSpeed Terminal velocity of the piano. Nullable, if null it will default to 10f
     * @param damage How much damage the piano will do when hitting a target. Nullable, if null it will default to 10
     * @return The newly created piano projectile
     */
    public static GrandPianoProjectile SpawnPiano(@Nullable Entity parent, Vec3 startingPos, @Nonnull Level level, double spawnHeight, @Nullable Float gravity, @Nullable Float maxFallSpeed, @Nullable Integer damage){
        GrandPianoProjectile piano = JarEntities.PIANO.get().create(level);
        assert piano != null;
        piano.setGravity(gravity != null ? gravity : defaultGravity);
        piano.setMaxFallSpeed(maxFallSpeed != null ? maxFallSpeed : defaultMaxFallSpeed);
        piano.setDamage(damage != null ? damage : defaultDamage);
        piano.setOwner(parent);
        piano.moveTo(startingPos.add(0, spawnHeight, 0));
        RandomSource random = RandomSource.create();
        piano.setYRot(random.nextInt(-180, 181));
        System.out.println("paino y rots: " + piano.getYRot() + ", " + piano.yRotO);
        piano.setState(state.falling);
        level.addFreshEntity(piano);
        return piano;
    }
    /**
     * Method to spawn a piano above a position or target. Uses an Entity rather than a Vec3 and Level argument
     * @param parent The "Owner" of the piano, Nullable. The Owner cannot be hurt by the piano
     * @param victim The target the piano is aiming for
     * @param spawnHeight How high above the spawn position will the piano spawn
     * @param gravity How fast the piano will pick up speed when falling. Nullable, if null it will default to 0.05f
     * @param maxFallSpeed Terminal velocity of the piano. Nullable, if null it will default to 10f
     * @param damage How much damage the piano will do when hitting a target. Nullable, if null it will default to 10
     * @return The newly created piano projectile
     */
    public static GrandPianoProjectile SpawnPiano(@Nullable Entity parent, Entity victim, double spawnHeight, @Nullable Float gravity, @Nullable Float maxFallSpeed, @Nullable Integer damage){
        return SpawnPiano(parent, victim.position(), victim.level(), spawnHeight, gravity, maxFallSpeed, damage);
    }
    /**
     * Shorthand to spawn a piano above a position or target, setting Gravity, MaxFallSpeed, and Damage to the default
     * @param parent The "Owner" of the piano, Nullable. The Owner cannot be hurt by the piano
     * @param startingPos The starting position of the piano, basically the desired landing position.
     *                    You may feed in null here IF the parent is not null
     * @param level The level for the entity to be created in, cannot be null for obvious reasons
     * @param spawnHeight How high above the spawn position will the piano spawn
     * @return The newly created piano projectile
     */
    public static GrandPianoProjectile SpawnPiano(@Nullable Entity parent, Vec3 startingPos, Level level, double spawnHeight){
        return SpawnPiano(parent, startingPos, level, spawnHeight, null, null, null);
    }
    /**
     * Shorthand to spawn a piano above a position or target, setting Gravity, MaxFallSpeed, and Damage to the default
     * @param parent The "Owner" of the piano, Nullable. The Owner cannot be hurt by the piano
     * @param victim The target the piano is aiming for
     * @param spawnHeight How high above the spawn position will the piano spawn
     * @return The newly created piano projectile
     */
    public static GrandPianoProjectile SpawnPiano(@Nullable Entity parent, Entity victim, double spawnHeight){
        return SpawnPiano(parent, victim.position(), victim.level(), spawnHeight, null, null, null);
    }

    @Override
    public void tick() {
        super.tick();
        if (getState() != 0){
            ManageVelocity();
            HitChecker();
        }
        if (getState() == 2){
            Age();
            if (getAge() > MaxAge){
                setState(3);
            }
        }
        if (getState() == 3){
            this.discard();
        }

        for (Entity E : trappedEntities){
            TrapEntityTick(E);
        }
    }
    public void ManageVelocity(){
        if (!level().getBlockState(blockPosition()).entityCanStandOn(level(), blockPosition(), this)){
            setDeltaMovement(getDeltaMovement().add(0, getDeltaMovement().y > -getMaxFallSpeed() ? -getGravity() : 0, 0));
        }
        else{
            setDeltaMovement(Vec3.ZERO);
        }
        setPos(position().add(getDeltaMovement()));
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
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        boolean shouldCrash = false;
        Level level = level();
        AABB AOEaabb = getBoundingBox().inflate(1.5f);
        for (BlockPos pos : BlockPos.betweenClosed((int)AOEaabb.minX, (int)AOEaabb.minY, (int)AOEaabb.minZ, (int)AOEaabb.maxX, (int)AOEaabb.maxY, (int)AOEaabb.maxZ)){
            BlockState state = level.getBlockState(pos);
            if (isStateBreakable(state.getBlock())){
                level.removeBlock(pos, false);
                level.levelEvent(2001, pos, Block.getId(level.getBlockState(pos)));
                level.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS);
                continue;
            }
            else if (state.isAir()) continue;
            shouldCrash = true;
        }
        if (shouldCrash) this.CRASH();
    }
    protected boolean isStateBreakable(Block block){
        return block instanceof AbstractGlassBlock || block instanceof LeavesBlock || block instanceof BambooStalkBlock ||
                block instanceof BambooSaplingBlock;
    }

    private final ArrayList<Entity> trappedEntities = new ArrayList<>();

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        Entity hitEntity = result.getEntity();
        if (this.canHitEntity(hitEntity) && !trappedEntities.contains(hitEntity)){
            trappedEntities.add(hitEntity);
        }
    }

    public void CRASH(){
        if (getState() != 1) return;
        setState(2);
        setDeltaMovement(Vec3.ZERO);
        setPos(position().add(0, 1, 0));
        playSound(JarSounds.PIANO_CRASH.get(), 5f, 1f);
        SpawnCrashParticles();

        Level level = level();
        if (level instanceof ServerLevel){
            AABB AOEaabb = getBoundingBox().inflate(2f);
            for (LivingEntity LE : level.getEntitiesOfClass(LivingEntity.class, AOEaabb, this::canHitEntity)){
                LE.invulnerableTime = 0;
                LE.hurt(JarDamageTypes.piano(LE), getDamage());
                trappedEntities.add(LE);
                if (ShouldThePianoBreakTheFabricOfReality && LE instanceof Player){
                    CrashTheFuckingGame();
                }
            }
            for (Entity E : trappedEntities){
                if (E instanceof LivingEntity LE && canHitEntity(LE)){
                    LE.invulnerableTime = 0;
                    LE.hurt(JarDamageTypes.piano(LE), getDamage());
                    if (ShouldThePianoBreakTheFabricOfReality && LE instanceof Player){
                        CrashTheFuckingGame();
                    }
                }
            }
        }
    }

    private static void CrashTheFuckingGame() {
        throw new RuntimeException("The Piano has broken the fabric of reality, fuck you.");
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity target) {
        return (getOwner() == null || target.getUUID() != getOwner().getUUID()) && super.canHitEntity(target);
    }

    private void SpawnCrashParticles() {
        int x = Mth.floor(this.getX());
        int y = Mth.floor(this.getY());
        int z = Mth.floor(this.getZ());
        Level world = this.level();
        RandomSource rand = this.random;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < rand.nextIntBetweenInclusive(80, 100); ++i) {
            blockpos$mutableblockpos.set(x + Mth.nextInt(rand, (int) (-getBbWidth() / 4), (int) (getBbWidth() / 4)), y + Mth.nextInt(rand, 0, (int)(getBbHeight() / 2)), z + Mth.nextInt(rand, (int) (-getBbWidth() / 4), (int) (getBbWidth() / 4)));
            BlockState blockstate = world.getBlockState(blockpos$mutableblockpos);
            if (!blockstate.isSolidRender(world, blockpos$mutableblockpos)) {
                world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, blockpos$mutableblockpos.getX(),
                        blockpos$mutableblockpos.getY(),
                        blockpos$mutableblockpos.getZ(),
                        rand.nextDouble() * (rand.nextBoolean() ? 1 : -1) / 5,
                        rand.nextDouble() / 5,
                        rand.nextDouble() * (rand.nextBoolean() ? 1 : -1) / 5);
            }
        }
    }

    public void TrapEntityTick(Entity toTrap){
        toTrap.setDeltaMovement(getDeltaMovement());
        if (getState() == 1) toTrap.setPos(position().add(0, -1, 0));
        toTrap.setSprinting(false);
    }
}
