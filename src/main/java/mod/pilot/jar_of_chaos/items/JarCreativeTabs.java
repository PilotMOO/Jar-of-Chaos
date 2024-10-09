package mod.pilot.jar_of_chaos.items;

import mod.pilot.jar_of_chaos.JarOfChaos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class JarCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JarOfChaos.MOD_ID);
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    public static final RegistryObject<CreativeModeTab> JAR_TAB = CREATIVE_MODE_TABS.register("entomophobia_tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 3).icon(() -> new ItemStack(JarItems.JAR.get()))
                    .title(Component.translatable("creativetab.jar_tab"))
                    .displayItems((something, register) ->{
                        register.accept(JarItems.JAR.get());
                        register.accept(JarItems.EXPLODE_WAND.get());
                        register.accept(JarItems.RANDOM_EXPLODE_WAND.get());
                    })
                    .build());
}
