package me.untouchedodin0.privatemines.storage;


import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.plugin.WorldEditHook;
import me.untouchedodin0.privatemines.hook.plugin.WorldEditHook.WorldEditWorldIO;
import me.untouchedodin0.privatemines.mine.MineBlocks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class SchematicStorage {
    private final Map<String, MineBlocks> mineBlocksMap = new HashMap<>();

    public MineBlocks computeThenAdd(File file) {
        WorldEditWorldIO worldIO = HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO();

        MineBlocks mineBlocks = worldIO.findRelativePoints(file);
        mineBlocksMap.put(file.getName().toLowerCase(), mineBlocks);
        return mineBlocks;
    }

    public MineBlocks get(File file) {
        return mineBlocksMap.get(file.getName().toLowerCase());
    }
}
