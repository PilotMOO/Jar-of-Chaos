package mod.pilot.jar_of_chaos.systems;

import mod.pilot.jar_of_chaos.sound.JarSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatteringTeethSoundManager {
    private static final HashMap<BlockPos, Integer> currentSoundHashmaps = new HashMap<>();
    private static ArrayList<BlockPos> getSoundPositions(){
        return new ArrayList<>(currentSoundHashmaps.keySet());
    }
    public static void Flush(){
        currentSoundHashmaps.clear();
    }
    public static void tick(){
        for (BlockPos position : getSoundPositions()){
            int age = currentSoundHashmaps.get(position);
            if (age < 55){
                currentSoundHashmaps.replace(position, age + 1);
            } else currentSoundHashmaps.remove(position);
        }
    }
    public static void RequestSoundAt(BlockPos position, ServerLevel server){
        if (!currentSoundHashmaps.containsKey(position)) {
            for (BlockPos existing : getSoundPositions()){
                if (existing.distToCenterSqr(position.getX(), position.getY(), position.getZ()) < 100){
                    return;
                }
            }
            currentSoundHashmaps.put(position, 0);
            server.playSound(null, position, JarSounds.CHATTERING_TEETH.get(), SoundSource.HOSTILE, 1, 1);
        }
    }
}
