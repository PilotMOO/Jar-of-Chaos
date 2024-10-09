package mod.pilot.jar_of_chaos.damagetypes;

import mod.pilot.jar_of_chaos.JarOfChaos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class JarDamageTypes {
    public static ResourceKey<DamageType> create(String id){
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(JarOfChaos.MOD_ID, id));
    }

    public static DamageSource damageSource(Entity entity, ResourceKey<DamageType> registryKey){
        return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(registryKey));
    }
    public static DamageSource damageSource(Entity entity, ResourceKey<DamageType> registryKey, @Nullable Entity entity2){
        return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(registryKey), entity2);
    }

    public static final ResourceKey<DamageType> PIANO1 = create("piano1");
    public static final ResourceKey<DamageType> PIANO2 = create("piano2");
    public static final ResourceKey<DamageType> PIANO3 = create("piano3");

    public static DamageSource piano(LivingEntity entity){
        switch (entity.getRandom().nextIntBetweenInclusive(1, 3)){
            default -> {
                return damageSource(entity, PIANO1, entity);
            }
            case 2 -> {
                return damageSource(entity, PIANO2, entity);
            }
            case 3 -> {
                return damageSource(entity, PIANO3, entity);
            }
        }
    }
}
