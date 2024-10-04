package mod.pilot.jar_of_chaos.events;

import mod.pilot.jar_of_chaos.JarOfChaos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = JarOfChaos.MOD_ID)

public class JarForgeEventHandler {
    private static ServerLevel Overworld;
    public static @NotNull ServerLevel getServer(){
        return Overworld;
    }

    @SubscribeEvent
    public static void GetServerFromStarting(ServerStartingEvent event){
        Overworld = event.getServer().overworld();
    }
}
