package mod.pilot.jar_of_chaos.effects;

import mod.pilot.jar_of_chaos.JarOfChaos;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class JarEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS
            = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, JarOfChaos.MOD_ID);

    public static final RegistryObject<MobEffect> SPLAT = MOB_EFFECTS.register("splat",
            SplatEffect::new);
    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
