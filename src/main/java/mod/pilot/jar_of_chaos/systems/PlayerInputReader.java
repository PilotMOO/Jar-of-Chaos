package mod.pilot.jar_of_chaos.systems;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class PlayerInputReader {
    public static void Setup(){
        MinecraftForge.EVENT_BUS.addListener(PlayerInputReader::RegisterPlayerInputOnJoin);
        MinecraftForge.EVENT_BUS.addListener(PlayerInputReader::UnregisterPlayerInputsOnLeave);
        MinecraftForge.EVENT_BUS.addListener(PlayerInputReader::ServerEndCleaning);
    }

    private static final HashMap<Player, Input> playerInputMap = new HashMap<>();
    private static ArrayList<Player> getAllRegisteredPlayers(){
        return new ArrayList<>(playerInputMap.keySet());
    }
    private static final ArrayList<Player> que = new ArrayList<>();
    private static ArrayList<Player> getAllQuedPlayers(){
        return new ArrayList<>(que);
    }
    public static @Nullable Input locateOrQueInputReaderToMap(Player player){
        if (!playerInputMap.containsKey(player)) {
            System.out.println("Player is not in the map!");
            if (player instanceof LocalPlayer lPlayer){
                System.out.println("player is a local player!");
                playerInputMap.put(lPlayer, lPlayer.input);

                for (Player qued : getAllQuedPlayers()){
                    if (qued.getGameProfile() == lPlayer.getGameProfile()){
                        System.out.println("Located player with identical game profile in que, unqueing...");
                        playerInputMap.put(qued, lPlayer.input);
                        que.remove(qued);
                    }
                }
                return lPlayer.input;
            } else {
                System.out.println("Player is NOT a local player");
                for (Player p : getAllRegisteredPlayers()){
                    if (p instanceof LocalPlayer lP && lP.getGameProfile() == player.getGameProfile()){
                        playerInputMap.put(player, lP.input);
                        System.out.println("Located a local player already registered with an identical game profile!");
                        return lP.input;
                    }
                }
                System.out.println("No players with identical game profiles were located, queing this player for later...");
                que.add(player);
            }
        } else {
            System.out.println("Player is in the map");
            return playerInputMap.get(player);
        }
        return null;
    }
    public static Optional<Input> getInputFor(Player p){
        return Optional.ofNullable(playerInputMap.computeIfAbsent(p, PlayerInputReader::locateOrQueInputReaderToMap));
    }

    private static void RegisterPlayerInputOnJoin(EntityJoinLevelEvent event){
        if (event.getEntity() instanceof Player p){
            locateOrQueInputReaderToMap(p);
            System.out.println("attempted to register player to input map");
        }
    }
    private static void UnregisterPlayerInputsOnLeave(EntityLeaveLevelEvent event){
        if (event.getEntity() instanceof Player player){
            playerInputMap.remove(player);
            que.remove(player);
        }
    }
    private static void ServerEndCleaning(ServerStoppedEvent event){
        playerInputMap.clear();
        que.clear();
        System.out.println("[PLAYER INPUT READER] Clearing all tracked player input readers!");
    }
}
