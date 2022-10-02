package me.marquez.cgapi;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomGlowPlugin extends JavaPlugin {

    private final List<GlowCondition> conditions = new ArrayList<>();

    @Override
    public void onEnable() {
        if(getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().warning("Can not found dependency ProtocolLib.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        FileConfiguration spigot = YamlConfiguration.loadConfiguration(new File("spigot.yml"));
        CustomGlowAPI.ENTITY_VIEW_RANGE = spigot.getInt("world-settings.default.entity-tracking-range.players");

        registerPacketAdapter();

        //Default Conditional
        conditions.add(new GlowCondition() {
           @Nullable
           @Override
           public Entity getGlowingEntity(UUID uuid, Player receiver) {
               Entity entity = Bukkit.getEntity(uuid);
               if (entity != null && CustomGlowAPI.isGlowing(uuid, receiver)) {
                   return entity;
               }
               return null;
           }

           @Override
           public boolean isGlowingById(int entityId, Player receiver) {
               return CustomGlowAPI.isGlowing(entityId, receiver);
           }
       });

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

    private void registerPacketAdapter() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(this, PacketType.Play.Server.ENTITY_METADATA) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.isPlayerTemporary()) return;
                        PacketContainer container = event.getPacket();
                        int entityId = container.getIntegers().read(0);
                        for(GlowCondition cond : conditions) {
                            if(cond.isGlowingById(entityId, event.getPlayer())) {
                                List<WrappedWatchableObject> value = container.getWatchableCollectionModifier().read(0);
                                value.stream().filter(v -> v.getIndex() == 0).forEach(v -> v.setValue((byte) ((byte) v.getValue() | 0x40)));
                                container.getWatchableCollectionModifier().write(0, value);
                                break;
                            }
                        }
                    }
                }
        );
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(this, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if(event.isPlayerTemporary()) return;
                        PacketContainer container = event.getPacket();
                        UUID uuid = container.getUUIDs().read(0);
                        Player player = event.getPlayer();
                        for(GlowCondition cond : conditions) {
                            Entity entity = cond.getGlowingEntity(uuid, player);
                            if(entity != null) {
                                CustomGlowAPI.applyGlowing(uuid, entity, player);
                                break;
                            }
                        }
                    }
                }
        );
    }

    public void registerCondition(GlowCondition condition) {
        conditions.add(condition);
    }
}
