package mod.pilot.jar_of_chaos.systems.SlimeRain;

import mod.pilot.jar_of_chaos.entities.mobs.KingSlimeEntity;

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
    public static void Clear(){
        activeKingSlimes.clear();
        kingBossEventMap.clear();
        System.out.println("[KING SLIME BOSS EVENT MANAGER] Cleared out all tracked bosses and events!");
    }
    public static void removeFromList(KingSlimeEntity kSlime){
        activeKingSlimes.remove(kSlime);
        kingBossEventMap.remove(kSlime);
    }
    public static ArrayList<KingSlimeEntity> getActiveKingSlimes(){return new ArrayList<>(activeKingSlimes);}
}
