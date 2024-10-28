package me.untouchedodin0.privatemines.mine;


import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.plugin.WorldEditHook;
import me.untouchedodin0.privatemines.hook.plugin.WorldEditHook.WorldEditWorldIO;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class SchematicStorage {
    private final Map<String, SchematicInformation> mineBlocksMap = new HashMap<>();

    public SchematicInformation computeThenAdd(File file) {
        WorldEditWorldIO worldIO = HookHandler.getHookHandler()
                .get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                .getWorldEditWorldIO();

        SchematicInformation schematicInformation = worldIO.findRelativePoints(file);
        mineBlocksMap.put(file.getName().toLowerCase(), schematicInformation);
        return schematicInformation;
    }

    public SchematicInformation get(File file) {
        return mineBlocksMap.get(file.getName().toLowerCase());
    }
}
