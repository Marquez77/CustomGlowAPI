package me.marquez.cgapi;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface GlowCondition {

    boolean isGlowing(UUID uuid, Player receiver);

}
