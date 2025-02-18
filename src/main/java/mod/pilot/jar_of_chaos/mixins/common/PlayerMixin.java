package mod.pilot.jar_of_chaos.mixins.common;

import mod.pilot.jar_of_chaos.systems.PlayerGeloid.GeloidManager;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Unique private static final EntityDimensions GELOID_CROUCH_DIMENSIONS = new EntityDimensions(0.75f, 0.5f, true);
    @Inject(method = "getDimensions", at = @At(value = "HEAD"), cancellable = true)
    public void InjectGeloidCrouchScale(Pose pPose, CallbackInfoReturnable<EntityDimensions> cir){
        if (pPose == Pose.CROUCHING && GeloidManager.isActiveGeloid(this)){
            cir.setReturnValue(GELOID_CROUCH_DIMENSIONS);
        }
    }
    @Inject(method = "getStandingEyeHeight", at = @At(value = "HEAD"), cancellable = true)
    public void InjectGeloidCrouchEyeHeight(Pose pPose, EntityDimensions pSize, CallbackInfoReturnable<Float> cir){
        if (pPose == Pose.CROUCHING && GeloidManager.isActiveGeloid(this)){
            cir.setReturnValue(0.45f);
        }
    }
}
