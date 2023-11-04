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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CustomGlowAPI {

    private CustomGlowAPI() {}
    protected static int ENTITY_VIEW_RANGE = 48;

    //AUTO REGISTER ENTITY_ID WITH UUID
    private static final Map<Pair<UUID, Player>, GlowColor> entities = new HashMap<>();

    public static boolean isGlowing(UUID uuid, Player receiver) {
        return entities.containsKey(new Pair<>(uuid, receiver));
    }

    public static void setGlowing(@Nonnull UUID uuid, @Nonnull GlowColor color, @Nonnull Player receiver) {
        entities.put(new Pair<>(uuid, receiver), color);
        Optional.ofNullable(Bukkit.getEntity(uuid)).ifPresent(entity -> {
            if(entity.getLocation().distance(receiver.getLocation()) <= ENTITY_VIEW_RANGE) {
                setGlowing(entity, color, receiver);
            }
        });
    }

    public static void setGlowing(@Nonnull UUID uuid, @Nonnull Player receiver) {
        setGlowing(uuid, GlowColor.WHITE, receiver);
    }

    public static void unsetGlowing(@Nonnull UUID uuid, @Nonnull Player receiver) {
        entities.remove(new Pair<>(uuid, receiver));
        Optional.ofNullable(Bukkit.getEntity(uuid)).ifPresent(entity -> {
            if(entity.getLocation().distance(receiver.getLocation()) <= ENTITY_VIEW_RANGE) {
                unsetGlowing(entity, receiver);
            }else {
                glowingData.remove(new Pair<>(entity.getEntityId(), receiver));
            }
        });
    }

    protected static void applyGlowing(UUID uuid, Entity entity, @Nullable GlowColor color, Player receiver) {
        if(glowingData.containsKey(new Pair<>(entity.getEntityId(), receiver))) return;
        Optional.ofNullable(entities.get(new Pair<>(uuid, receiver))).ifPresentOrElse(setColor -> setGlowing(entity, setColor, receiver), () -> setGlowing(entity, color == null ? GlowColor.WHITE : color, receiver));
    }

    //GENERIC GLOWING
    private static final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
    private static final Map<Pair<Integer, Player>, GlowColor> glowingData = new HashMap<>();

    public static boolean isGlowing(Entity entity, Player receiver) {
        return isGlowing(entity.getEntityId(), receiver);
    }

    public static boolean isGlowing(int entityId, Player receiver) {
        return glowingData.containsKey(new Pair<>(entityId, receiver));
    }

    @Nullable
    public static GlowColor getGlowingColor(Entity entity, Player receiver) {
        return getGlowingColor(entity.getEntityId(), receiver);
    }

    @Nullable
    public static GlowColor getGlowingColor(int entityId, Player receiver) {
        return glowingData.get(new Pair<>(entityId, receiver));
    }

    public static void setGlowing(@Nonnull Entity entity, @Nonnull GlowColor color, @Nonnull Player receiver) {
        applyTeam(entity, color, receiver);
        glowingData.put(new Pair<>(entity.getEntityId(), receiver), color);
        sendGlowingPacket(entity, receiver, true);
    }

    public static void setGlowing(@Nonnull Entity entity, @Nonnull Player receiver) {
        setGlowing(entity, GlowColor.WHITE, receiver);
    }

    public static void unsetGlowing(@Nonnull Entity entity, @Nonnull Player receiver) {
        applyTeam(entity, GlowColor.WHITE, receiver);
        glowingData.remove(new Pair<>(entity.getEntityId(), receiver));
        sendGlowingPacket(entity, receiver, false);
    }

    private static void sendGlowingPacket(Entity entity, Player receiver, boolean isGlowing) {
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entity.getEntityId());
        applyGlowing(packet, entity, isGlowing);
        manager.sendServerPacket(receiver.getPlayer(), packet);
    }

    private static void applyGlowing(PacketContainer packet, Entity entity, boolean isGlowing) {
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        dataWatcher.setEntity(entity);
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        dataWatcher.setObject(0, serializer, isGlowing ? (byte)0x40 : (byte)0x00);
        packet.getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
    }

    //SCOREBOARD TEAM FOR COLOR GLOWING
    private static final Map<GlowColor, PlayerTeam> teams = new HashMap<>();
    private static final Map<Player, Set<GlowColor>> knownColors = new HashMap<>();

    protected static void clearColors(Player player) {
        knownColors.remove(player);
    }

    private static void applyTeam(@Nonnull Entity entity, @Nonnull GlowColor color, @Nonnull Player receiver) {
        List<Packet<?>> packets = new ArrayList<>();
        String target = entity instanceof OfflinePlayer ? entity.getName() : entity.getUniqueId().toString();
        if(isGlowing(entity, receiver)) {
            GlowColor prevColor = getGlowingColor(entity, receiver);
            if(prevColor != null && prevColor != GlowColor.WHITE && prevColor != color) {
                PlayerTeam team = teams.get(prevColor);
                ClientboundSetPlayerTeamPacket removePacket = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, target, ClientboundSetPlayerTeamPacket.Action.REMOVE);
                packets.add(removePacket);
            }
        }
        if(color != GlowColor.WHITE) {
            PlayerTeam team = teams.compute(color, (key, value) -> {
                Scoreboard scoreboard = new Scoreboard();
                PlayerTeam playerTeam = new PlayerTeam(scoreboard, "CGAPI#" + color.name());
                playerTeam.setPlayerPrefix(Component.literal(new String(new char[]{'§', color.getCode()})));
                playerTeam.setColor(ChatFormatting.getByName(color.name()));
                return playerTeam;
            });
            Set<GlowColor> colors = knownColors.computeIfAbsent(receiver, value -> new HashSet<>());
            if (!colors.contains(color)) {
                colors.add(color);
                ClientboundSetPlayerTeamPacket createTeamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
                packets.add(createTeamPacket);
            }
            ClientboundSetPlayerTeamPacket addPacket = ClientboundSetPlayerTeamPacket.createPlayerPacket(team, target, ClientboundSetPlayerTeamPacket.Action.ADD);
            packets.add(addPacket);
        }
        sendPackets(receiver, packets.toArray(Packet[]::new));
    }

    private static void sendPackets(Player player, Packet<?>... packets) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer)player).getHandle().connection;
        Arrays.stream(packets).forEach(connection::send);
    }

}
