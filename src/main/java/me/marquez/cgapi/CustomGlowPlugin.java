package me.marquez.cgapi;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CustomGlowPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if(getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().warning("Can not found dependency ProtocolLib.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ProtocolLibrary.getProtocolManager().addPacketListener(
            new PacketAdapter(this, PacketType.Play.Server.ENTITY_METADATA) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    if (event.isPlayerTemporary()) return;
                    PacketContainer container = event.getPacket();
                    int entityId = container.getIntegers().read(0);
                    if (CustomGlowAPI.isGlowing(entityId, event.getPlayer())) {
                        List<WrappedWatchableObject> value = container.getWatchableCollectionModifier().read(0);
                        value.stream().filter(v -> v.getIndex() == 0).forEach(v -> v.setValue((byte) ((int) v.getValue() | 0x40)));
                        container.getWatchableCollectionModifier().write(0, value);
                    }
                }
            }
        );
        getLogger().info("Enabled CustomGlowPlugin.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled CustomGlowPlugin.");
    }
}
