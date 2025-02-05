package mod.pilot.jar_of_chaos.items;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.blocks.JarBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class JarCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JarOfChaos.MOD_ID);
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

    public static final RegistryObject<CreativeModeTab> JAR_TAB = CREATIVE_MODE_TABS.register("joc_tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 3).icon(() -> new ItemStack(JarItems.JAR.get()))
                    .title(Component.translatable("creativetab.jar_tab"))
                    .displayItems((something, register) ->{
                        register.accept(JarItems.JAR.get());
                        register.accept(JarItems.JESTER_BOW.get());
                        register.accept(JarItems.CHATTER_CANNON.get());

                        register.accept(JarItems.CHATTERING_TEETH_SPAWN.get());

                        register.accept(JarBlocks.SLIME_LAYER.get());

                        register.accept(JarItems.SLIME_EFFIGY.get());
                        register.accept(JarItems.SLIME_ARROW.get());
                    })
                    .build());
}
