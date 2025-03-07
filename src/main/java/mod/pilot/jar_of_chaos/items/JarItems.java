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
    public static final RegistryObject<Item> CHATTER_CANNON = ITEMS.register("chatter_cannon",
            () -> new ChatterCannonItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CHATTERING_TEETH_SPAWN = ITEMS.register("chattering_teeth_spawn",
            () -> new ChatteringTeethSpawn(new Item.Properties()));

    public static final RegistryObject<Item> SLIME_EFFIGY = ITEMS.register("slime_effigy",
            () -> new SlimeEffigyItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SLIME_ARROW = ITEMS.register("slime_arrow",
            () -> new SlimeArrowItem(new Item.Properties()));

    public static final RegistryObject<Item> KING_SLIME_CROWN = ITEMS.register("king_slime_crown",
            () -> new KingSlimeCrown(new Item.Properties()));

    public static final RegistryObject<Item> GELOID_MANAGER = ITEMS.register("geloid_wand",
            () -> new GeloidSetter(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MAGIC_STICK_WAND = ITEMS.register("magic_stick_wand",
            () -> new MagicStickWand(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
