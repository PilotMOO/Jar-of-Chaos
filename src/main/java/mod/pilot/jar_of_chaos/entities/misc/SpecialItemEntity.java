package mod.pilot.jar_of_chaos.entities.misc;

import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.items.JarItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

//NOTE! YOU HAVE TO INVOKE SpecialItemEntity.setSize(float); AFTER CREATING THIS ENTITY IF YOU DO NOT USE ONE OF THE HELPER METHODS
public class SpecialItemEntity extends ItemEntity {
    public SpecialItemEntity(EntityType<? extends ItemEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setGlowingTag(true);
    }
    public static SpecialItemEntity CreateAt(Level level, Vec3 pos, ItemStack itemStack){
        return CreateAt(level, pos, itemStack, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_SIZE);
    }
    public static SpecialItemEntity CreateAt(Level level, Vec3 pos, ItemStack itemStack, int color, int endColor, float size){
        return CreateAt(level, pos, itemStack, color, endColor, size, 0);
    }
    public static SpecialItemEntity CreateAt(Level level, Vec3 pos, ItemStack itemStack, int color, int endColor, float size, int pickupDelay){
        SpecialItemEntity sItem = new SpecialItemEntity(JarEntities.SPECIAL_ITEM.get(), level);

        sItem.setPos(pos);
        sItem.setItem(itemStack);
        sItem.setPickUpDelay(pickupDelay);
        level.addFreshEntity(sItem);
        sItem.setColor(color);
        sItem.setEndColor(endColor);
        sItem.setSize(size);
        return sItem;
    }
    public static SpecialItemEntity CreateMagicStick(Level level, Vec3 pos){
        SpecialItemEntity sItem = new SpecialItemEntity(JarEntities.SPECIAL_ITEM.get(), level);
        sItem.setPos(pos);
        sItem.setItem(new ItemStack(Items.STICK));
        sItem.setPickUpDelay(40);
        level.addFreshEntity(sItem);
        sItem.setSize(DEFAULT_SIZE);
        return sItem;

    }
    public static SpecialItemEntity CreateUahh(Level level, Vec3 pos){
        SpecialItemEntity sItem = new SpecialItemEntity(JarEntities.SPECIAL_ITEM.get(), level);
        sItem.setPos(pos);
        sItem.setItem(new ItemStack(JarItems.KING_SLIME_CROWN.get()));
        sItem.setPickUpDelay(40);
        level.addFreshEntity(sItem);
        sItem.setSize(DEFAULT_SIZE);
        return sItem;
    }
    public static final EntityDataAccessor<Integer> BeamColor = SynchedEntityData.defineId(SpecialItemEntity.class, EntityDataSerializers.INT);
    public int getColor(){return entityData.get(BeamColor);}
    public void setColor(int color){entityData.set(BeamColor, color);}
    public static final EntityDataAccessor<Integer> BeamEndColor = SynchedEntityData.defineId(SpecialItemEntity.class, EntityDataSerializers.INT);
    public int getEndColor(){return entityData.get(BeamEndColor);}
    public void setEndColor(int color){entityData.set(BeamEndColor, color);}
    public static final EntityDataAccessor<Float> Size = SynchedEntityData.defineId(SpecialItemEntity.class, EntityDataSerializers.FLOAT);
    public float getSize(){return entityData.get(Size);}
    public void setSize(float size){entityData.set(Size, size);}
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BeamColor", getColor());
        tag.putInt("BeamEndColor", getEndColor());
        tag.putFloat("Size", getSize());
    }
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setColor(tag.getInt("BeamColor"));
        setEndColor(tag.getInt("BeamEndColor"));
        setSize(tag.getFloat("Size"));
    }
    @Override
    protected void defineSynchedData() {
        //16711680 is red
        //16777215 is white

        super.defineSynchedData();
        entityData.define(BeamColor, 16777215);
        entityData.define(BeamEndColor, 16777215); //default this to smth else
        entityData.define(Size, 0.0f);
    }
    public static final int DEFAULT_COLOR = 16777215;
    public static final float DEFAULT_SIZE = 1f;

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pPose) {
        return new EntityDimensions(0.15f + getSize() / 12.5f, 0.15f + getSize() / 12.5f, false);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (Size.equals(key)){
            refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }
}
