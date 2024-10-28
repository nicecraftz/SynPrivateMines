package me.untouchedodin0.privatemines.mine.world;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

public class EmptyWorldGenerator extends ChunkGenerator {

    @Override
    @NotNull
    public ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int x, int z,
                                       @NotNull BiomeGrid biome) {
        return createChunkData(world);
    }
}