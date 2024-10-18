package mod.pilot.jar_of_chaos.items;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.items.custom.*;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class JarItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JarOfChaos.MOD_ID);

    public static final RegistryObject<Item> JAR = ITEMS.register("jar_of_chaos",
            () -> new JarItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> JESTER_BOW = ITEMS.register("jester_bow",
            () -> new JesterBowItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> EXPLODE_WAND = ITEMS.register("explode_wand",
            () -> new ExplodeEventWandTest(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RANDOM_EXPLODE_WAND = ITEMS.register("random_explode_wand",
            () -> new DisplacedContinuousExplosionWand(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PIANO_WAND = ITEMS.register("piano_wand",
            () -> new PianoWand(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> STAR_WAND = ITEMS.register("star_wand",
            () -> new StarParticleWand(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
