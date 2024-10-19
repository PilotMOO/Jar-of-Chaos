package mod.pilot.jar_of_chaos.entities.projectiles;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.particles.JarParticles;
import mod.pilot.jar_of_chaos.systems.JesterArrowEvents.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.ArrayList;

public class JesterArrowProjectile extends AbstractArrow implements GeoEntity {
    public JesterArrowProjectile(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setCritTrail(JarParticles.STAR_PARTICLE.get());
    }
    private JesterArrowProjectile(EntityType<? extends AbstractArrow> pEntityType, Level pLevel, ArrayList<Class<? extends JesterArrowEvent>> toAvoid) {
        super(pEntityType, pLevel);
        setCritTrail(JarParticles.STAR_PARTICLE.get());
        GenerateRandomEvent(toAvoid);
    }
    public static JesterArrowProjectile Create(Level level){
        JesterArrowProjectile J = new JesterArrowProjectile(JarEntities.JESTER_ARROW.get(), level);
        J.GenerateRandomEvent(null);
        return J;
    }
    public static JesterArrowProjectile CreateWithBlacklist(Level level, ArrayList<Class<? extends JesterArrowEvent>> toAvoid){
        return new JesterArrowProjectile(JarEntities.JESTER_ARROW.get(), level, toAvoid);
    }
    public static final EntityDataAccessor<Boolean> Crit = SynchedEntityData.defineId(JesterArrowProjectile.class, EntityDataSerializers.BOOLEAN);
    public boolean getCrit(){return entityData.get(Crit);}
    public void setCrit(boolean flag) {entityData.set(Crit, flag);}
    public static final EntityDataAccessor<Integer> EventIndex = SynchedEntityData.defineId(JesterArrowProjectile.class, EntityDataSerializers.INT);
    public int getEventIndex(){return entityData.get(EventIndex);}
    public void setEventIndex(int index) {entityData.set(EventIndex, index);}
    public static final EntityDataAccessor<Boolean> EventFired = SynchedEntityData.defineId(JesterArrowProjectile.class, EntityDataSerializers.BOOLEAN);
    public boolean getEventFired(){return entityData.get(EventFired);}
    public void setEventFired(boolean flag) {entityData.set(EventFired, flag);}
    public JesterArrowEvent Event;

    private ParticleOptions particleTrail;
    public void setCritTrail(ParticleOptions trail){
        particleTrail = trail;
    }
    public ParticleOptions getCritTrail(){
        return particleTrail;
    }
    public void CreateEventFromIndex(int index){
        Event = JesterArrowEventManager.createNewEventFromIndex(index, this);
    }
    public void GenerateRandomEvent(@Nullable ArrayList<Class<? extends JesterArrowEvent>> toAvoid){
        Pair<JesterArrowEvent, Integer> pair = JesterArrowEventManager.createNewRandomEvent(this, toAvoid);
        if (pair != null){
            Event = pair.getA();
            setEventIndex(pair.getB());
        }
    }

    @Override
    public void setCritArrow(boolean pCritArrow) {
        setCrit(pCritArrow);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Crit", entityData.get(Crit));
        tag.putInt("EventIndex", entityData.get(EventIndex));
        tag.putBoolean("EventFired", entityData.get(EventFired));
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(Crit, tag.getBoolean("Crit"));
        entityData.set(EventIndex, tag.getInt("EventIndex"));
        entityData.set(EventFired, tag.getBoolean("EventFired"));
        CreateEventFromIndex(getEventIndex());
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(Crit, false);
        this.entityData.define(EventIndex, -1);
        this.entityData.define(EventFired, false);
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "ArrowManager", event ->{
            Vec3 delta = getDeltaMovement();
            event.setControllerSpeed((float)((Math.abs(delta.x) + Math.abs(delta.y) + Math.abs(delta.z)) / 3));
            if (getDeltaMovement() != Vec3.ZERO){
                return event.setAndContinue(RawAnimation.begin().thenLoop("Fire"));
            }
            if (inGround){
                event.setControllerSpeed(0);
            }
            return null;
        }));
    }
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public void tick() {
        super.tick();
        if (Event != null) Event.Tick();

        if (level() instanceof ServerLevel server && getCrit() && !inGround) {
            server.sendParticles(getCritTrail(),
                    getX() + (random.nextDouble() / 4), getY() + (random.nextDouble() / 4), getZ() + (random.nextDouble() / 4),
                    random.nextInt(1, 3),
                    random.nextDouble() * (random.nextBoolean() ? 1 : -1),
                    random.nextDouble() * (random.nextBoolean() ? 1 : -1),
                    random.nextDouble() * (random.nextBoolean() ? 1 : -1),
                    0);
        }

        if (tickCount > 1200){
            discard();
        }
    }
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        if (Event != null) Event.OnHitEntity(result.getEntity());
    }
    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        if (Event != null) Event.OnHitBlock(result.getBlockPos(), level().getBlockState(result.getBlockPos()));
    }
}
