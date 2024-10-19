package mod.pilot.jar_of_chaos.systems.JesterArrowEvents;

import mod.pilot.jar_of_chaos.Config;
import mod.pilot.jar_of_chaos.entities.AI.FishProjectileGoal;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.entities.projectiles.JesterArrowProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES;


public abstract class JesterArrowEvent {
    /**
     * Abstract class for all events called by the Jester Arrow projectile shot out from the Jester's Bow.
     * When creating a new event, make sure to make and call a public static void that registers the event inside <code>JesterArrowEventManager</code>
     * using <code>JesterArrowEventManager.RegisterEvent(JesterArrowEvent)</code>
     * @param parent The arrow that the event is being called from. Unassigned (nulled) arrows will never have their methods called
     */
    private JesterArrowEvent(JesterArrowProjectile parent){
        Arrow = parent;
    }
    public final JesterArrowProjectile Arrow;
    public Vec3 getPos(){
        return Arrow.position();
    }
    public int getAge(){
        return Arrow.tickCount;
    }
    public void KillEvent(){
        Arrow.Event = null;
        Arrow.setEventIndex(-1);
    }
    public void KillArrow(){
        Arrow.discard();
    }
    public Level level(){
        return Arrow.level();
    }

    public void OnSpawn(){
        Arrow.setEventFired(true);
    }
    public abstract void Tick();
    public abstract void OnHitBlock(BlockPos bPos, BlockState bState);
    public abstract void OnHitEntity(Entity target);

    public abstract JesterArrowEvent Create(JesterArrowProjectile arrow);

    public static class CombustEvent extends JesterArrowEvent{
        public CombustEvent(JesterArrowProjectile parent) {
            super(parent);
            if (parent != null){
                parent.setCritTrail(ParticleTypes.FLAME);
            }
        }

        @Override
        public void OnSpawn() {
            super.OnSpawn();
            level().playSound(null, getPos().x, getPos().y, getPos().z, SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 1f, 0.75f);
        }

        @Override
        public void Tick() {
        }

        @Override
        public void OnHitBlock(BlockPos bPos, BlockState bState) {
            level().explode(Arrow, null, new ExplosionDamageCalculator(), getPos(), 0.5f, true, Level.ExplosionInteraction.MOB);
            KillArrow();
        }

        @Override
        public void OnHitEntity(Entity target) {
            level().explode(Arrow, null, new ExplosionDamageCalculator(), getPos(), 0.5f, true, Level.ExplosionInteraction.MOB);
            target.setSecondsOnFire(20);
            target.setDeltaMovement(target.getDeltaMovement().add(0, 1.5, 0));
            KillArrow();
        }

        public static void Register(){
            JesterArrowEventManager.RegisterEvent(new CombustEvent(null));
        }

        @Override
        public JesterArrowEvent Create(JesterArrowProjectile arrow) {
            return new CombustEvent(arrow);
        }
    }
    public static class VolleyEvent extends JesterArrowEvent{
        public VolleyEvent(JesterArrowProjectile parent) {
            super(parent);
        }

        @Override
        public void OnSpawn() {
            super.OnSpawn();
            RandomSource random = RandomSource.create();
            for (int i = 0; i < 8; i++){
                JesterArrowProjectile JArrow = new JesterArrowProjectile(JarEntities.JESTER_ARROW.get(), level()){
                    @Override
                    protected void onHitEntity(@NotNull EntityHitResult result) {
                        super.onHitEntity(result);
                        result.getEntity().invulnerableTime = 0;
                        discard();
                    }

                    @Override
                    protected boolean canHitEntity(@NotNull Entity target) {
                        return super.canHitEntity(target) && target != getOwner();
                    }

                    @Override
                    public @NotNull EntityDimensions getDimensions(@NotNull Pose pPose) {
                        return new EntityDimensions(getBbWidth() / 2, getBbHeight() / 2, false);
                    }

                    @Override
                    protected void onHitBlock(@NotNull BlockHitResult result) {
                        super.onHitBlock(result);
                    }
                };
                Entity owner = Arrow.getOwner();
                JArrow.setOwner(owner);
                JArrow.setBaseDamage(JArrow.getBaseDamage() / 4);
                JArrow.setCrit(Arrow.getCrit());
                JArrow.copyPosition(Arrow);
                JArrow.setDeltaMovement(Arrow.getDeltaMovement().offsetRandom(random, 1f));
                level().addFreshEntity(JArrow);
            }
        }

        @Override
        public void Tick() {
            KillEvent(); KillArrow();
        }

        @Override
        public void OnHitBlock(BlockPos bPos, BlockState bState){}
        @Override
        public void OnHitEntity(Entity target) {}

        public static void Register(){
            JesterArrowEventManager.RegisterEvent(new VolleyEvent(null));
        }

        @Override
        public JesterArrowEvent Create(JesterArrowProjectile arrow) {
            return new VolleyEvent(arrow);
        }
    }
    public static class FishEvent extends JesterArrowEvent{
        public FishEvent(JesterArrowProjectile parent) {
            super(parent);
        }

        private static final String EntityID = Config.SERVER.fish_entity.get();

        @Override
        public void OnSpawn() {
            super.OnSpawn();

            Entity E = ENTITY_TYPES.getValue(new ResourceLocation(EntityID)).create(level());
            if (E == null) return;
            if (E instanceof Mob m){
                m.goalSelector.addGoal(0, new FishProjectileGoal(m, Arrow.getDeltaMovement()));
            }
            E.copyPosition(Arrow);
            E.setPos(E.position().add(Arrow.getForward()));
            E.invulnerableTime = 20;
            level().addFreshEntity(E);
        }

        @Override
        public void Tick() {KillEvent(); KillArrow();}

        @Override
        public void OnHitBlock(BlockPos bPos, BlockState bState) {}

        @Override
        public void OnHitEntity(Entity target) {}

        public static void Register(){
            JesterArrowEventManager.RegisterEvent(new FishEvent(null));
        }

        @Override
        public JesterArrowEvent Create(JesterArrowProjectile arrow) {
            return new FishEvent(arrow);
        }
    }
    public static class TridentEvent extends JesterArrowEvent{
        public TridentEvent(JesterArrowProjectile parent) {
            super(parent);
        }

        @Override
        public void OnSpawn() {
            super.OnSpawn();

            ThrownTrident trident = new ThrownTrident(EntityType.TRIDENT, level());
            trident.setOwner(Arrow.getOwner());
            trident.copyPosition(Arrow);
            trident.setDeltaMovement(Arrow.getDeltaMovement());
            trident.pickup = AbstractArrow.Pickup.DISALLOWED;
            level().addFreshEntity(trident);
            level().playSound(null, Arrow.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS);
        }

        @Override
        public void Tick() {
            KillEvent(); KillArrow();
        }

        @Override
        public void OnHitBlock(BlockPos bPos, BlockState bState) {}

        @Override
        public void OnHitEntity(Entity target) {}

        public static void Register(){
            JesterArrowEventManager.RegisterEvent(new TridentEvent(null));
        }

        @Override
        public JesterArrowEvent Create(JesterArrowProjectile arrow) {
            return new TridentEvent(arrow);
        }
    }
    public static class FreezeEvent extends JesterArrowEvent{
        public FreezeEvent(JesterArrowProjectile parent) {
            super(parent);
            if (parent != null){
                parent.setCritTrail(ParticleTypes.SNOWFLAKE);
            }
        }

        @Override
        public void Tick() {}
        @Override
        public void OnHitBlock(BlockPos bPos, BlockState bState) {
            FreezeNearby(bPos);
        }

        @Override
        public void OnHitEntity(Entity target) {
            FreezeNearby(target.blockPosition());
        }

        private void FreezeNearby(BlockPos bPos) {
            if (level() instanceof ServerLevel server) {
                boolean soundFlag = false;
                AABB freezeAOE = AABB.ofSize(bPos.getCenter(), 3, 3, 3);
                for (BlockPos bPos2 : BlockPos.betweenClosed((int) freezeAOE.minX, (int) freezeAOE.minY, (int) freezeAOE.minZ,
                        (int) freezeAOE.maxX, (int) freezeAOE.maxY, (int) freezeAOE.maxZ)) {
                    BlockState bState2 = level().getBlockState(bPos2);
                    if (bState2.getFluidState().is(Fluids.WATER)) {
                        boolean blockFlag = server.setBlock(bPos2, Blocks.ICE.defaultBlockState(), 3);
                        if (soundFlag) continue;
                        soundFlag = blockFlag;
                    }
                    else if (bState2.isAir() && !level().getBlockState(bPos2.below()).isAir()){
                        server.setBlock(bPos2, Blocks.SNOW.defaultBlockState(), 3);
                    }
                }
                for (LivingEntity LE : level().getEntitiesOfClass(LivingEntity.class, freezeAOE)){
                    LE.setTicksFrozen(200);
                    LE.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
                    LE.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200));
                    LE.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100));
                    soundFlag = true;
                }
                if (soundFlag){
                    server.playSound(null, bPos, SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS);
                }
            }
        }

        public static void Register(){
            JesterArrowEventManager.RegisterEvent(new FreezeEvent(null));
        }

        @Override
        public JesterArrowEvent Create(JesterArrowProjectile arrow) {
            return new FreezeEvent(arrow);
        }
    }
    public static class LightningEvent extends JesterArrowEvent{
        public LightningEvent(JesterArrowProjectile parent) {
            super(parent);
        }

        @Override
        public void Tick() {}

        @Override
        public void OnHitBlock(BlockPos bPos, BlockState bState) {
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level());
            bolt.setPos(bPos.above().getCenter());
            level().addFreshEntity(bolt);
        }

        @Override
        public void OnHitEntity(Entity target) {
            target.invulnerableTime = 0;
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level());
            bolt.setPos(target.position());
            level().addFreshEntity(bolt);
        }

        public static void Register(){
            JesterArrowEventManager.RegisterEvent(new LightningEvent(null));
        }

        @Override
        public JesterArrowEvent Create(JesterArrowProjectile arrow) {
            return new LightningEvent(arrow);
        }
    }
}
