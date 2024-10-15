package mod.pilot.jar_of_chaos.enchantments;

import mod.pilot.jar_of_chaos.JarOfChaos;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class JarEnchants {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, JarOfChaos.MOD_ID);
    private static final String DisabledEnchantMSG = " [THIS ENCHANTMENT IS DISABLED VIA THE CONFIG, IT WILL NOT DO ANYTHING]";
    public static String getDisabledEnchantMSG(){
        return DisabledEnchantMSG;
    }

    public static final RegistryObject<Enchantment> PIANOISM = ENCHANTMENTS.register("pianoism", PianoismEnchant::new);

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
