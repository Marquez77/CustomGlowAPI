package me.marquez.cgapi;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomGlowPlugin extends JavaPlugin implements CommandExecutor {

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

//        registerPacketAdapter();

        //Default Conditional
        conditions.add((uuid, receiver) -> {
               Entity entity = Bukkit.getEntity(uuid);
               if(entity != null && CustomGlowAPI.isGlowing(uuid, receiver)) {
                   return GlowColor.WHITE;
               }
               return null;
           }
       );

        getCommand("gapi").setExecutor(this);

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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            sender.sendMessage("/gapi glow <player|uuid> <color> <viewer>");
        }else if(args[0].equalsIgnoreCase("glow")) {
            if(args.length < 4) {
                sender.sendMessage("Invalid arguments.");
                return true;
            }
            Entity entity = null;
            if(args[1].length() > 16) {
                entity = Bukkit.getEntity(UUID.fromString(args[1]));
            }else {
                entity = Bukkit.getPlayer(args[1]);
            }
            if(entity == null) {
                sender.sendMessage("Invalid entity");
                return true;
            }
            GlowColor color = GlowColor.valueOf(args[2]);
            Player viewer = Bukkit.getPlayer(args[3]);
            if(viewer == null) {
                sender.sendMessage("Invalid viewer");
                return true;
            }
            if(CustomGlowAPI.isGlowing(entity, viewer)) {
                CustomGlowAPI.unsetGlowing(entity, viewer);
                sender.sendMessage("Unset glowing");
            }else {
                CustomGlowAPI.setGlowing(entity, color, viewer);
                sender.sendMessage("Set glowing");
            }
        }
        return true;
    }

//    private void registerPacketAdapter() {
//        ProtocolLibrary.getProtocolManager().addPacketListener(
//                new PacketAdapter(this, PacketType.Play.Server.ENTITY_METADATA) {
//                    @Override
//                    public void onPacketSending(PacketEvent event) {
//                        if (event.isPlayerTemporary()) return;
//                        PacketContainer container = event.getPacket();
//                        int entityId = container.getIntegers().read(0);
//                        if(CustomGlowAPI.isGlowing(entityId, event.getPlayer())) {
//                            System.out.println("onPacketSending: " + container);
//                            System.out.println("values: " + container.getWatchableCollectionModifier().getValues());
////                            List<WrappedWatchableObject> value = container.getWatchableCollectionModifier().read(0);
////                            value.stream().filter(v -> v.getIndex() == 0).forEach(v -> v.setValue((byte) ((byte) v.getValue() | 0x40)));
////                            container.getWatchableCollectionModifier().write(0, value);
//                        }
//                    }
//                }
//        );
//        ProtocolLibrary.getProtocolManager().addPacketListener(
//                new PacketAdapter(this, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
//                    @Override
//                    public void onPacketSending(PacketEvent event) {
//                        if(event.isPlayerTemporary()) return;
//                        PacketContainer container = event.getPacket();
//                        UUID uuid = container.getUUIDs().read(0);
//                        Player player = event.getPlayer();
//                        for(GlowCondition cond : conditions) {
//                            GlowColor color = cond.getGlowingColor(uuid, player);
//                            if(color != null) {
//                                Entity entity = Bukkit.getEntity(uuid);
//                                if(entity != null) {
//                                    CustomGlowAPI.applyGlowing(uuid, entity, color, player);
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//        );
//    }

    public void registerCondition(GlowCondition condition) {
        conditions.add(condition);
    }
}
