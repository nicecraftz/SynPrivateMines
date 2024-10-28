package me.untouchedodin0.privatemines.mine;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public record MineBlocks(Vector spawnLocation, BoundingBox miningArea) {
    public MineBlocks {
        if (spawnLocation == null) throw new IllegalArgumentException("Spawn location cannot be null");
        if (miningArea == null) throw new IllegalArgumentException("Area cannot be null");
    }
}
