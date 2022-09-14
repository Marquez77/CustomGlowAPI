package me.marquez.cgapi;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Color;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CustomGlowAPI {

    private CustomGlowAPI() {}

    private static final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
    private static final Map<Pair<Integer, Player>, Color> glowingData = new HashMap<>();

    public static boolean isGlowing(Entity entity, Player receiver) {
        return isGlowing(entity.getEntityId(), receiver);
    }

    public static boolean isGlowing(int entityId, Player receiver) {
        return glowingData.containsKey(new Pair<>(entityId, receiver));
    }

    public static void setGlowing(Entity entity, Player receiver) {
        int entityId = entity.getEntityId();
        glowingData.put(new Pair<>(entityId, receiver), Color.WHITE);
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entityId);
        applyGlowing(packet, entity);
        try {
            manager.sendServerPacket(receiver.getPlayer(), packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void applyGlowing(PacketContainer packet, Entity entity) {
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        dataWatcher.setEntity(entity);
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        dataWatcher.setObject(0, serializer, (byte)0x40);
        packet.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
    }
}
