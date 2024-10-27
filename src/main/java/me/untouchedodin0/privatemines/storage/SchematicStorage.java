package me.untouchedodin0.privatemines.storage;


import me.untouchedodin0.privatemines.hook.WorldIO;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SchematicStorage {
    private final WorldIO worldIO;
    private final Map<File, WorldIO.MineBlocks> mineBlocksMap = new HashMap<>();

    public SchematicStorage(WorldIO worldIO) {
        this.worldIO = worldIO;
    }

    public void computeThenAdd(File file) {
        WorldIO.MineBlocks mineBlocks = worldIO.findRelativePoints(file);
        mineBlocksMap.putIfAbsent(file, mineBlocks);
    }

    public WorldIO.MineBlocks get(File file) {
        return mineBlocksMap.get(file);
    }
}
