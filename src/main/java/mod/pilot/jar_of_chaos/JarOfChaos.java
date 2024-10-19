package mod.pilot.jar_of_chaos;

import com.mojang.logging.LogUtils;
import mod.pilot.jar_of_chaos.enchantments.JarEnchants;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.items.JarCreativeTabs;
import mod.pilot.jar_of_chaos.items.JarItems;
import mod.pilot.jar_of_chaos.particles.JarParticles;
import mod.pilot.jar_of_chaos.sound.JarSounds;
import mod.pilot.jar_of_chaos.systems.JesterArrowEvents.JesterArrowEventManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(JarOfChaos.MOD_ID)
public class JarOfChaos
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "jar_of_chaos";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public JarOfChaos()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);
        JarItems.register(modEventBus);
        JarCreativeTabs.register(modEventBus);
        JarEntities.register(modEventBus);
        JarSounds.register(modEventBus);
        JarEnchants.register(modEventBus);
        JarParticles.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SERVER_SPEC, "JoC_Config.toml");
        Config.loadConfig(Config.SERVER_SPEC, FMLPaths.CONFIGDIR.get().resolve("JoC_Config.toml").toString());

        JesterArrowEventManager.RegisterAllEvents();
    }
}
