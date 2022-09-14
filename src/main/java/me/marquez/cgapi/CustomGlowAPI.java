package me.marquez.cgapi;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CustomGlowAPI {

    private CustomGlowAPI() {}

    private static final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
    private static final Map<Pair<Integer, Player>, ChatColor> glowingData = new HashMap<>();

    public static boolean isGlowing(Entity entity, Player receiver) {
        return isGlowing(entity.getEntityId(), receiver);
    }

    public static boolean isGlowing(int entityId, Player receiver) {
        return glowingData.containsKey(new Pair<>(entityId, receiver));
    }

    public static void setGlowing(Entity entity, Player receiver) {
        int entityId = entity.getEntityId();
        glowingData.put(new Pair<>(entityId, receiver), ChatColor.WHITE);
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


    //SCOREBOARD
    private static Map<ChatColor, PlayerTeam> teams = new HashMap<>();
    private static Set<Pair<Player, ChatColor>> knownChatColors = new HashSet<>();

    protected static void clearColors(Player player) {
        knownChatColors.remove(player);
    }

    private static void applyTeam(Entity entity, ChatColor color, Player receiver) {
        PlayerTeam team = teams.compute(color, (key, value) -> {
            Scoreboard scoreboard = new Scoreboard();
            PlayerTeam playerTeam = new PlayerTeam(scoreboard, "CGAPI#" + color.name());
            playerTeam.setPlayerPrefix(Component.literal(color.toString()));
            playerTeam.setPlayerSuffix(Component.literal(""));
            playerTeam.setColor(ChatFormatting.getByName(color.name()));
            return playerTeam;
        });
        Pair<Player, ChatColor> key = new Pair<>(receiver, color);
        if(!knownChatColors.contains(key)) {
            knownChatColors.add(key);
            ClientboundSetPlayerTeamPacket createTeamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
            sendPackets(receiver, createTeamPacket);
        }
        ClientboundSetPlayerTeamPacket addPacket = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, entity instanceof OfflinePlayer ? entity.getName() : entity.getUniqueId().toString(), ClientboundSetPlayerTeamPacket.Action.ADD);
        sendPackets(receiver, addPacket);
    }

    private static void sendPackets(Player player, Packet<?>... packets) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer)player).getHandle().connection;
        Arrays.stream(packets).forEach(connection::send);
    }

}
