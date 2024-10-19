package mod.pilot.jar_of_chaos;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

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

        public final ForgeConfigSpec.ConfigValue<Boolean> should_pianos_crash_harder;

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

            builder.push("Stupid configs options");
            should_pianos_crash_harder = builder.define("Should the Grand Piano crash the entire FUCKING game if it hits a player?", false);
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
