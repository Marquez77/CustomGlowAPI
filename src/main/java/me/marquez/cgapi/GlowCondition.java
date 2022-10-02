package me.marquez.cgapi;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import javax.annotation.Nullable;
import java.util.UUID;

public interface GlowCondition {

    @Nullable
    Entity getGlowingEntity(UUID uuid, Player receiver);

    boolean isGlowingById(int entityId, Player receiver);

}
