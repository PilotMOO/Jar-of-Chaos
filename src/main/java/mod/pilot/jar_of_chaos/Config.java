package mod.pilot.jar_of_chaos;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.io.File;
import java.util.List;

@Mod.EventBusSubscriber(modid = JarOfChaos.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static class Server{
        public final ForgeConfigSpec.ConfigValue<Boolean> enable_piano_enchant;
        public final ForgeConfigSpec.ConfigValue<Double> piano_chance;

        public final ForgeConfigSpec.ConfigValue<String> fish_entity;

        public final ForgeConfigSpec.ConfigValue<Double> slime_rain_chance;
        public final ForgeConfigSpec.ConfigValue<Integer> slime_rain_poi_kills;
        public final ForgeConfigSpec.ConfigValue<Integer> slime_rain_poi_range;
        public final ForgeConfigSpec.ConfigValue<Integer> king_slime_starting_size;
        public final ForgeConfigSpec.ConfigValue<Integer> slime_rain_min_duration;
        public final ForgeConfigSpec.ConfigValue<Integer> slime_rain_max_duration;

        public final ForgeConfigSpec.ConfigValue<Boolean> pickup_teeth;
        public final ForgeConfigSpec.ConfigValue<Boolean> teeth_pvp;
        public final ForgeConfigSpec.ConfigValue<Integer> teeth_age;

        public final ForgeConfigSpec.ConfigValue<Boolean> should_pianos_crash_harder;
        public final ForgeConfigSpec.ConfigValue<Boolean> should_kirby;

        public Server(ForgeConfigSpec.Builder builder){
            builder.push("Jar of Chaos config");

            builder.push("Enchantment Configuration");
            enable_piano_enchant = builder.define("Enable/Disable \"Pianoism\" enchantment:", true);
            piano_chance = builder.defineInRange("Base chance for a piano to spawn from the Pianoism enchantment," +
                    "this value is multiplied by the level", 0.1d, 0d, 1d);
            builder.pop();

            builder.push("Weapon Configuration");

            builder.push("Jester's Bow (and arrows)");
            fish_entity = builder.define("The entity ID of what mob will be spawned when the \"Fish\" Jester Arrow event is called",
                    "minecraft:salmon");
            builder.pop();

            builder.pop();

            builder.push("Slime Rain Configuration");
            this.slime_rain_chance = builder.defineInRange("Chance for the slime rain to occur every natural rain storm (set to 0 to disable natural slime rains)",
                    0.2d, 0d, 1d);
            this.slime_rain_poi_kills = builder.defineInRange("How many kills are required for King Slime to spawn",
                    100, 0, Integer.MAX_VALUE);
            this.slime_rain_poi_range = builder.defineInRange("How many blocks a given Slime Rain POI spans",
                    256, 0, Integer.MAX_VALUE);
            this.king_slime_starting_size = builder.defineInRange("The starting size of King Slime when he first spawns in",
                    200, 1, Integer.MAX_VALUE);
            this.slime_rain_min_duration = builder.defineInRange("The MINIMUM duration of a slime rain event (CANNOT be higher than max duration)",
                    10800, 0, Integer.MAX_VALUE - 1);
            this.slime_rain_max_duration = builder.defineInRange("The MAXIMUM duration of a slime rain event (CANNOT be lower than min duration)",
                    18000, 1, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Entity Configuration");
            teeth_age = builder.defineInRange("How long Chattering Teeth can exist for before being removed", 2400, 1, Integer.MAX_VALUE);
            pickup_teeth = builder.define("Should you be able to pick up other people's chattering teeth?", false);
            teeth_pvp = builder.define("Should Chattering Teeth owned by players attack other players?", false);
            builder.pop();

            builder.push("Stupid configs options");
            should_pianos_crash_harder = builder.define("Should the Grand Piano crash the entire FUCKING game if it hits a player?", false);
            should_kirby = builder.define("Should Kirby fall?", false);
        }
    }

    static {
        Pair<Server, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = commonSpecPair.getLeft();
        SERVER_SPEC = commonSpecPair.getRight();
    }

    public static void loadConfig(ForgeConfigSpec config, String path) {
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        config.setConfig(file);
    }
}
