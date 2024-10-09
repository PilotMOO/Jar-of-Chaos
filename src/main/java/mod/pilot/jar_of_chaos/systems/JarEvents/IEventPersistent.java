package mod.pilot.jar_of_chaos.systems.JarEvents;

import net.minecraft.nbt.CompoundTag;

public interface IEventPersistent {
    default boolean isPersistent(){
        assert this instanceof JarEvent;
        return !((JarEvent) this).isInstant() && !((JarEvent)this).isExpired();
    }
    void PackagePersistentData(CompoundTag tag, int tagIndex);
    <T extends JarEvent> T UnpackPersistentData(String baseID);
}
