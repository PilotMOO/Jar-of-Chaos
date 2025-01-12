package mod.pilot.jar_of_chaos.data.worlddata;

import mod.pilot.jar_of_chaos.JarOfChaos;
import mod.pilot.jar_of_chaos.systems.SlimeRain.SlimeRainManager;
import mod.pilot.jar_of_chaos.systems.SlimeRain.SlimeRainPOI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
            System.out.println("Set slime rain duration to " + data.slimeRainDuration + " because that's what it was in the world data");
            if (data.slimeRainDuration > 0){
                SlimePOIPacker.start().unpack(tag);
            }
        }
        if (tag.contains("slime_rain_finalizing")){
            SlimeRainManager.finalizing = tag.getBoolean("slime_rain_finalizing");
        }
        data.setDirty();
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putInt("slime_rain_duration", slimeRainDuration);

        if (slimeRainDuration > 0){
            SlimePOIPacker.start().pack(tag);
            tag.putBoolean("slime_rain_finalizing", SlimeRainManager.finalizing);
        }

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

    private static class SlimePOIPacker{
        public static SlimePOIPacker start(){return new SlimePOIPacker();}
        private SlimePOIPacker(){
            builder = new StringBuilder();
        }
        private final StringBuilder builder;
        private void cleanBuilder(){builder.setLength(0);}

        public void pack(CompoundTag tag){
            ArrayList<SlimeRainPOI> toPack = SlimeRainManager.getPOIs();
            if (toPack == null){
                System.err.println("[SLIME RAIN UNPACKER LOG] The POIs ArrayList in SlimeRainManager was null!");
                return;
            }

            final String sPOI = "SlimePOI";
            final int baseLength = sPOI.length();
            int index = 0;
            cleanBuilder();
            builder.append(sPOI);
            for (SlimeRainPOI poi : toPack){
                builder.append(index++);
                final int length = builder.toString().length();

                tag.putInt(builder.append("kills").toString(), poi.getKills()); builder.setLength(length);
                tag.putInt(builder.append("range").toString(), poi.range); builder.setLength(length);

                tag.putDouble(builder.append("x").toString(), poi.position.x); builder.setLength(length);
                tag.putDouble(builder.append("y").toString(), poi.position.y); builder.setLength(length);
                tag.putDouble(builder.append("z").toString(), poi.position.z); builder.setLength(length);
                builder.setLength(baseLength);
            }
            System.out.println("Packed up " + index + " SlimeRainPOI's!");
        }
        public void unpack(CompoundTag tag){
            SlimeRainManager.SetupPOIList();

            final String sPOI = "SlimePOI";
            final int baseLength = sPOI.length();
            cleanBuilder();
            builder.append(sPOI);
            int count = 0;
            for (int i = 0; true; i++){
                final int length = builder.append(i).toString().length();
                if (!tag.contains(builder.append("kills").toString())) break;

                int kills = tag.getInt(builder.toString()); builder.setLength(length);
                int range = tag.getInt(builder.append("range").toString()); builder.setLength(length);

                double x = tag.getDouble(builder.append("x").toString()); builder.setLength(length);
                double y = tag.getDouble(builder.append("y").toString()); builder.setLength(length);
                double z = tag.getDouble(builder.append("z").toString()); builder.setLength(baseLength);
                Vec3 pos = new Vec3(x, y, z);

                SlimeRainManager.quietAddToPOIs(SlimeRainPOI.createFromBlueprint(kills, range, pos));
                count++;
            }
            System.out.println("Unpacked " + count + " SlimeRainPOI's!");
        }
    }
}
