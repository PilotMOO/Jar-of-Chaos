package mod.pilot.jar_of_chaos;

import mod.pilot.jar_of_chaos.blocks.JarBlocks;
import mod.pilot.jar_of_chaos.data.worlddata.JarGeneralSaveData;
import mod.pilot.jar_of_chaos.effects.JarEffects;
import mod.pilot.jar_of_chaos.enchantments.JarEnchants;
import mod.pilot.jar_of_chaos.entities.JarEntities;
import mod.pilot.jar_of_chaos.items.JarCreativeTabs;
import mod.pilot.jar_of_chaos.items.JarItems;
import mod.pilot.jar_of_chaos.particles.JarParticles;
import mod.pilot.jar_of_chaos.sound.JarSounds;
import mod.pilot.jar_of_chaos.systems.ChatteringTeethSoundManager;
import mod.pilot.jar_of_chaos.systems.JarEvents.JarEventHandler;
import mod.pilot.jar_of_chaos.systems.JesterArrowEvents.JesterArrowEventManager;
import mod.pilot.jar_of_chaos.systems.ModelDisplay.InWorldDisplayManager;
import mod.pilot.jar_of_chaos.systems.ModelDisplay.client.GenericModelHub;
import mod.pilot.jar_of_chaos.systems.PlayerGeloid.GeloidManager;
import mod.pilot.jar_of_chaos.systems.SlimeRain.SlimeRainManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(JarOfChaos.MOD_ID)
public class JarOfChaos
{
    public static final String MOD_ID = "jar_of_chaos";

    public static JarGeneralSaveData activeData;
    public JarOfChaos()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(this);
        //MinecraftForge.EVENT_BUS.addListener(JarOfChaos::FinalizeLoading);
        JarItems.register(modEventBus);
        JarBlocks.register(modEventBus);
        JarCreativeTabs.register(modEventBus);
        JarEntities.register(modEventBus);
        JarSounds.register(modEventBus);
        JarEnchants.register(modEventBus);
        JarParticles.register(modEventBus);
        JarEffects.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SERVER_SPEC, "JoC_config.toml");
        Config.loadConfig(Config.SERVER_SPEC, FMLPaths.CONFIGDIR.get().resolve("JoC_config.toml").toString());

        GenericModelHub.Setup();
        InWorldDisplayManager.Setup();
        JarEventHandler.Setup();
        JesterArrowEventManager.Setup();
        SlimeRainManager.Setup();
        GeloidManager.Setup();
        ChatteringTeethSoundManager.Setup();
    }

    public static void FinalizeLoading(FMLCommonSetupEvent event){
        System.out.println("Enqueuing work for finalizing init...");
        event.enqueueWork(GenericModelHub::FinalizeInit);
    }
}
