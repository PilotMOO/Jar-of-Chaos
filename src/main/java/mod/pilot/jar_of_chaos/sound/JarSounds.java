package mod.pilot.jar_of_chaos.sound;

import mod.pilot.jar_of_chaos.JarOfChaos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class JarSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JarOfChaos.MOD_ID);

    public static final RegistryObject<SoundEvent> PIANO_CRASH = registerSoundEvents("piano_crash");

    public static final ForgeSoundType TEST_SOUND_TYPE = new ForgeSoundType(1f, 1f,
            null, null, null, null, null); //Replace Null with actual sounds

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(JarOfChaos.MOD_ID, name)));
    }
    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}
