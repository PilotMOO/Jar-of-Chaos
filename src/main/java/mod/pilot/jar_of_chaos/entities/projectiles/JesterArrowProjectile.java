package mod.pilot.jar_of_chaos.entities.projectiles;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.util.AzureLibUtil;
import mod.pilot.jar_of_chaos.particles.JarParticles;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class JesterArrowProjectile extends AbstractArrow implements GeoEntity {
    public JesterArrowProjectile(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    public static final EntityDataAccessor<Boolean> Crit = SynchedEntityData.defineId(JesterArrowProjectile.class, EntityDataSerializers.BOOLEAN);
    public boolean getCrit(){return entityData.get(Crit);}
    public void setCrit(boolean flag) {entityData.set(Crit, flag);}

    @Override
    public void setCritArrow(boolean pCritArrow) {
        setCrit(pCritArrow);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Crit", entityData.get(Crit));
    }
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(Crit, tag.getBoolean("Crit"));
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(Crit, false);
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "ArrowManager", event ->{
            Vec3 delta = getDeltaMovement();
            event.setControllerSpeed((float)((Math.abs(delta.x) + Math.abs(delta.y) + Math.abs(delta.z)) / 3));
            if (getDeltaMovement() != Vec3.ZERO){
                return event.setAndContinue(RawAnimation.begin().thenLoop("Fire"));
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
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server && getCrit() && !inGround){
            server.sendParticles(JarParticles.STAR_PARTICLE.get(),
                    getX() + (random.nextDouble() / 4), getY() + (random.nextDouble() / 4), getZ() + (random.nextDouble() / 4),
                    random.nextInt(1, 3),
                    random.nextDouble() * (random.nextBoolean() ? 1 : -1),
                    random.nextDouble() * (random.nextBoolean() ? 1 : -1),
                    random.nextDouble() * (random.nextBoolean() ? 1 : -1),
                    random.nextDouble());
        }
    }
}
