package mod.pilot.jar_of_chaos.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class JarMathHelper {
    public static Vec3 getDirectionFromAToB(Entity target, Entity parent) {
        return parent.position().subtract(target.position()).normalize();
    }
    public static Vec3 getDirectionFromAToB(Vec3 target, Entity parent) {
        return parent.position().subtract(target).normalize();
    }
    public static Vec3 getDirectionFromAToB(Entity target, Vec3 parent) {
        return parent.subtract(target.position()).normalize();
    }
    public static Vec3 getDirectionFromAToB(Vec3 target, Vec3 parent) {
        return parent.subtract(target).normalize();
    }
    public static Vec3 getDirectionToAFromB(Entity target, Entity parent) {
        return target.position().subtract(parent.position()).normalize();
    }
    public static Vec3 getDirectionToAFromB(Vec3 target, Entity parent) {
        return target.subtract(parent.position()).normalize();
    }
    public static Vec3 getDirectionToAFromB(Entity target, Vec3 parent) {
        return target.position().subtract(parent).normalize();
    }
    public static Vec3 getDirectionToAFromB(Vec3 target, Vec3 parent) {
        return target.subtract(parent).normalize();
    }
}
