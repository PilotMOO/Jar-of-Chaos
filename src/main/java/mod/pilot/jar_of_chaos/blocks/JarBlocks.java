package mod.pilot.jar_of_chaos.blocks;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.blocks.custom.SlimeLayerBlock;
import mod.pilot.jar_of_chaos.items.JarItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class JarBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, JarOfChaos.MOD_ID);

    public static RegistryObject<Block> SLIME_LAYER = registryBlock("slime_layer",
            () -> new SlimeLayerBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)));

    private static <T extends Block> RegistryObject<T> registryBlock(String name, Supplier<T> block){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        RegisterBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block, J extends BlockItem> RegistryObject<T> registerBlockWithCustomItem(String name, Supplier<T> block, Supplier<J> blockItem){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        JarItems.ITEMS.register(name, blockItem);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> RegisterBlockItem(String name, RegistryObject<T> block) {
        return JarItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }
}
