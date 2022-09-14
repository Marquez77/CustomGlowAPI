package me.marquez.cgapi;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;

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
                        value.stream().filter(v -> v.getIndex() == 0).forEach(v -> v.setValue((byte) ((byte) v.getValue() | 0x40)));
                        container.getWatchableCollectionModifier().write(0, value);
                    }
                }
            }
        );
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(this, PacketType.Play.Server.SCOREBOARD_TEAM) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        PacketContainer packet = event.getPacket();
                        Optional optional = (Optional)packet.getModifier().read(3);
                        optional.ifPresent(o -> {
                            ClientboundSetPlayerTeamPacket.Parameters parameters = (ClientboundSetPlayerTeamPacket.Parameters) o;
                            if(!parameters.getDisplayName().toString().startsWith("literal{CIT-")) {
                                System.out.println("display: " + parameters.getDisplayName());
                                System.out.println("prefix: " + parameters.getPlayerPrefix());
                                System.out.println("suffix: " + parameters.getPlayerSuffix());
                                System.out.println("color: " + parameters.getColor());
                                System.out.println("options: " + parameters.getOptions());
                            }
                        });

//                        ClientboundSetPlayerTeamPacket.createPlayerPacket()
//                        PlayerTeam
                    }
                }
        );
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent e) {
                CustomGlowAPI.clearColors(e.getPlayer());
            }

        }, this);
        getLogger().info("Enabled CustomGlowPlugin.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled CustomGlowPlugin.");
    }
}
