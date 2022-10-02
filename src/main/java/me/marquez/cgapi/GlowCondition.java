package me.marquez.cgapi;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public interface GlowCondition {

    @Nullable
    ChatColor getGlowingColor(UUID uuid, Player receiver);

}
