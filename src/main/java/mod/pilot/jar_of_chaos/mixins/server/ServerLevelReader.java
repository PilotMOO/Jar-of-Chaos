package mod.pilot.jar_of_chaos.mixins.server;

import mod.pilot.jar_of_chaos.Config;
import mod.pilot.jar_of_chaos.events.JarForgeEventHandler;
import mod.pilot.jar_of_chaos.systems.SlimeRain.SlimeRainManager;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelReader extends Level implements WorldGenLevel {
    protected ServerLevelReader(WritableLevelData pLevelData, ResourceKey<Level> pDimension, RegistryAccess pRegistryAccess, Holder<DimensionType> pDimensionTypeRegistration, Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed, int pMaxChainedNeighborUpdates) {
        super(pLevelData, pDimension, pRegistryAccess, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed, pMaxChainedNeighborUpdates);
    }

    @Shadow @Final private ServerLevelData serverLevelData;
    @Unique private boolean jarOfChaos$rainStatePrior;

    @Unique
    private static int jarOfChaos$getThenClearWeatherClearTime(){
        int priorTime = JarForgeEventHandler.clearWeatherTime;
        JarForgeEventHandler.clearWeatherTime = -1;
        return priorTime;
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void preTickWeatherReader(BooleanSupplier pHasTimeLeft, CallbackInfo c_if){
        int newTime = jarOfChaos$getThenClearWeatherClearTime();
        if (newTime != -1){
            serverLevelData.setClearWeatherTime(newTime);
        }
        jarOfChaos$rainStatePrior = serverLevelData.isRaining();
    }
    @Inject(method = "tick", at = @At("RETURN"))
    private void postTickWeatherReader(BooleanSupplier pHasTimeLeft, CallbackInfo c_if){
        if (!jarOfChaos$rainStatePrior && serverLevelData.isRaining()){
            if (random.nextDouble() <= jarOfChaos$slimeRainChance){
                SlimeRainManager.StartSlimeRain(this.getLevel(), random.nextInt(jarOfChaos$minSlimeRainDuration, jarOfChaos$maxSlimeRainDuration), false);
            }
        }
    }

    @Unique private static final double jarOfChaos$slimeRainChance = Config.SERVER.slime_rain_chance.get();
    @Unique private static final int jarOfChaos$minSlimeRainDuration = Config.SERVER.slime_rain_min_duration.get();
    @Unique private static final int jarOfChaos$maxSlimeRainDuration = Config.SERVER.slime_rain_max_duration.get();
}
