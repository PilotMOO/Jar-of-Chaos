package mod.pilot.jar_of_chaos.items.custom;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.util.RenderUtils;
import mod.pilot.jar_of_chaos.Config;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.entities.mobs.ChatteringTeethEntity;
import mod.pilot.jar_of_chaos.items.JarItems;
import mod.pilot.jar_of_chaos.items.custom.client.ChatterCannonRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public class ChatterCannonItem extends ProjectileWeaponItem implements GeoItem {
    public ChatterCannonItem(Properties pProperties) {
        super(pProperties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private ChatterCannonRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new ChatterCannonRenderer();
                }
                return this.renderer;
            }
        });
    }
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "manager", event
                -> event.setAndContinue(RawAnimation.begin().thenLoop("empty")))
                .triggerableAnim("empty", RawAnimation.begin().thenLoop("empty"))
                .triggerableAnim("load", RawAnimation.begin().then("load", Animation.LoopType.PLAY_ONCE)
                        .thenLoop("charge"))
                .triggerableAnim("charge", RawAnimation.begin().thenLoop("charge"))
                .triggerableAnim("fire", RawAnimation.begin().then("fire", Animation.LoopType.PLAY_ONCE)
                        .thenLoop("empty")));
    }
    private final AnimatableInstanceCache cache = createInstanceCache(this);
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack) {
        return 20;
    }

    private enum ChatterType{
        empty,
        normal,
        explosive
    }
    public static byte getChatterType(ItemStack cannon){
        CompoundTag tag = cannon.getOrCreateTag();
        return tag.contains("ChatterType") ? tag.getByte("ChatterType") : 0;
    }
    public static void setChatterProjectile(ItemStack cannon, byte type){
        CompoundTag tag = cannon.getOrCreateTag();
        tag.putByte("ChatterType", type);
    }
    public static void setChatterProjectile(ItemStack cannon, ChatterType type){
        setChatterProjectile(cannon, (byte)type.ordinal());
    }

    @Override
    public int getDefaultProjectileRange() {
        return 4;
    }
    private static final double defaultVelocity = 2d;
    public static void setVelocity(ItemStack cannon, double velocity){
        CompoundTag tag = cannon.getOrCreateTag();
        tag.putDouble("Velocity", velocity);
    }
    public static double getVelocity(ItemStack cannon){
        CompoundTag tag = cannon.getOrCreateTag();
        return tag.contains("Velocity") ? tag.getDouble("Velocity") : defaultVelocity;
    }
    private static final double defaultYTrajectory = 0.5d;
    public static void setYTrajectory(ItemStack cannon, double newTrajectory){
        CompoundTag tag = cannon.getOrCreateTag();
        tag.putDouble("YTrajectory", newTrajectory);
    }
    public static double getYTrajectory(ItemStack cannon){
        CompoundTag tag = cannon.getOrCreateTag();
        return tag.contains("YTrajectory") ? tag.getDouble("YTrajectory") : defaultYTrajectory;
    }
    public static Vec3 getTrajectory(ItemStack cannon, LivingEntity owner){
        return owner.getForward().add(0, getYTrajectory(cannon), 0).scale(getVelocity(cannon));
    }

    public static boolean isLoaded(ItemStack cannon){
        CompoundTag tag = cannon.getOrCreateTag();
        return tag.contains("Loaded") && tag.getBoolean("Loaded");
    }
    private static void setLoaded(ItemStack cannon, boolean flag){
        CompoundTag tag = cannon.getOrCreateTag();
        tag.putBoolean("Loaded", flag);
    }
    public static void Load(ItemStack cannon, byte type, ItemStack projStack){
        setLoaded(cannon, true);
        setChatterProjectile(cannon, type);
        projStack.shrink(1);
    }
    public static void Unload(ItemStack cannon){
        setLoaded(cannon, false);
        setChatterProjectile(cannon, (byte)0);
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return ChatterCannonItem::isChatteringTeeth;
    }
    private static boolean isChatteringTeeth(ItemStack itemStack) {
        return itemStack.getItem() instanceof ChatteringTeethSpawn;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack cannon = player.getItemInHand(hand);
        if (isLoaded(cannon)) {
            if (level instanceof ServerLevel s && cannon.getItem() instanceof ChatterCannonItem){
                triggerAnim(player, GeoItem.getOrAssignId(cannon, s),
                        "manager", "fire");
            }
            FireChatter(level, player, cannon);
            return InteractionResultHolder.fail(cannon);
        } else if (!player.getProjectile(cannon).isEmpty()) {
            if (!isLoaded(cannon)) {
                player.startUsingItem(hand);
            }

            return InteractionResultHolder.consume(cannon);
        } else {
            return InteractionResultHolder.fail(cannon);
        }
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity user, @NotNull ItemStack cannon, int remainingDuration) {
        if (level instanceof ServerLevel s){
            triggerAnim(user, GeoItem.getOrAssignId(cannon, s),
                    "manager", "load");
        }

        if (remainingDuration == 15){
            level.playSound(null, user, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1f, 0.75f);
            Load(cannon, getChatterTypeFromFirstAmmo(cannon, user), user.getProjectile(cannon));
        }
        if (remainingDuration == 0){
            if (level instanceof ServerLevel s){
                triggerAnim(user, GeoItem.getOrAssignId(cannon, s),
                        "manager", "fire");
            }
            FireChatter(level, user, cannon);
        }
    }

    private static byte getChatterTypeFromFirstAmmo(ItemStack cannon, LivingEntity user) {
        ItemStack projStack = user.getProjectile(cannon);
        if (!isChatteringTeeth(projStack) && !(user instanceof Player p && p.isCreative())) return 0;
        //ToDo! make a chain of if...else to check if the item is an instance of other chatter spawns (explosive for example)
        return 1;
    }

    public static void FireChatter(Level level, LivingEntity owner, ItemStack cannon){
        EntityType<? extends ChatteringTeethEntity> chatterType = getChatterEntityFromType(getChatterType(cannon));
        if (chatterType == null) return;
        ChatteringTeethEntity C = chatterType.create(level);
        C.copyPosition(owner);
        C.setPos(C.position().add(owner.getForward().add(0, owner.getBbHeight() / 2, 0)));
        C.setOwnerUUID(owner);
        C.setDeltaMovement(getTrajectory(cannon, owner));
        level.addFreshEntity(C);
        level.playSound(null, owner, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1f, 0.5f);

        if (owner instanceof Player p){
            p.getCooldowns().addCooldown(JarItems.CHATTER_CANNON.get(), 10);
        }

        Unload(cannon);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack cannon, @NotNull Level level, @NotNull LivingEntity user, int timeCharged) {
        if (level instanceof ServerLevel s){
            triggerAnim(user, GeoItem.getOrAssignId(cannon, s), "manager", isLoaded(cannon) ? "charge" : "empty");
        }
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.SPYGLASS;
    }

    private static EntityType<? extends ChatteringTeethEntity> getChatterEntityFromType(byte type){
        switch (type){
            default : return null;
            case 1 : return JarEntities.CHATTERING_TEETH.get();
        }
    }
}
