package mod.pilot.jar_of_chaos.data.worlddata;

import mod.pilot.jar_of_chaos.JarOfChaos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class JarGeneralSaveData extends SavedData {
    public static final String NAME = JarOfChaos.MOD_ID + "_misc_world_data";

    public JarGeneralSaveData(){
        super();
        slimeRainDuration = 0;
    }
    public static void setActiveData(ServerLevel server){
        JarOfChaos.activeData = server.getDataStorage().computeIfAbsent(JarGeneralSaveData::load, JarGeneralSaveData::new, NAME);
        activeData().setDirty();
    }
    private static @NotNull JarGeneralSaveData activeData(){
        return JarOfChaos.activeData;
    }
    public static void Dirty(){
        activeData().setDirty();
    }

    public static JarGeneralSaveData load(CompoundTag tag){
        JarGeneralSaveData data = new JarGeneralSaveData();
        if (tag.contains("slime_rain_duration",99)){
            data.slimeRainDuration = tag.getInt("slime_rain_duration");
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putInt("slime_rain_duration", slimeRainDuration);

        return tag;
    }

    private int slimeRainDuration;
    public static int getSlimeRainDuration(){
        return activeData().slimeRainDuration;
    }
    public static boolean isSlimeRain(){
        return getSlimeRainDuration() > 0;
    }
    public static void setSlimeRainDuration(int value){
        activeData().slimeRainDuration = value;
        Dirty();
    }
    public static void tickSlimeRainDuration(){
        setSlimeRainDuration(getSlimeRainDuration() - 1);
    }
}
