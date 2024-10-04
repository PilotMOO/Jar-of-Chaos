package mod.pilot.jar_of_chaos.items;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.items.custom.JarItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class JarItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JarOfChaos.MOD_ID);

    public static final RegistryObject<Item> JAR = ITEMS.register("jar_of_chaos",
            () -> new JarItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
