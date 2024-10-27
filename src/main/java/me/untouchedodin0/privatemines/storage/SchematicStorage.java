package me.untouchedodin0.privatemines.storage;


import com.google.common.collect.ImmutableMap;
import me.untouchedodin0.privatemines.hook.WorldIO;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SchematicStorage {
    private final Map<File, WorldIO.MineBlocks> mineBlocksMap = new HashMap<>();

    public void add(File schematicFile, WorldIO.MineBlocks mineBlocks) {
        mineBlocksMap.putIfAbsent(schematicFile, mineBlocks);
    }

    public Map<File, WorldIO.MineBlocks> getMineBlocksMap() {
        return ImmutableMap.copyOf(mineBlocksMap);
    }

    public WorldIO.MineBlocks get(File file) {
        return mineBlocksMap.get(file);
    }
}
