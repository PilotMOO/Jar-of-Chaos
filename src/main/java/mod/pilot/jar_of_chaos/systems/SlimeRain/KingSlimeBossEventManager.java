package mod.pilot.jar_of_chaos.systems.SlimeRain;

import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;
import net.minecraft.world.BossEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class KingSlimeBossEventManager {
    public static final HashMap<KingSlimeEntity, UUID> kingBossEventMap = new HashMap<>();
    public static boolean checkBossEventsFor(KingSlimeEntity.BossEvent event){return checkBossEventsFor(event.getId());}
    public static boolean checkBossEventsFor(UUID eventUUID){
        return kingBossEventMap.containsValue(eventUUID);
    }

    private static final ArrayList<KingSlimeEntity> activeKingSlimes = new ArrayList<>();
    public static void addToList(KingSlimeEntity kSlime){
        activeKingSlimes.add(kSlime);
        kingBossEventMap.put(kSlime, kSlime.getBossEventID());
    }
    public static void flushList(){
        activeKingSlimes.clear();
        kingBossEventMap.clear();
    }
    public static void removeFromList(KingSlimeEntity kSlime){
        activeKingSlimes.remove(kSlime);
        kingBossEventMap.remove(kSlime);
    }
    public static ArrayList<KingSlimeEntity> getActiveKingSlimes(){return new ArrayList<>(activeKingSlimes);}
}
