package me.untouchedodin0.privatemines.utils.schematic;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.untouchedodin0.privatemines.hook.HookHandler;
import me.untouchedodin0.privatemines.hook.plugin.WorldEditHook;
import org.bukkit.Location;

import java.io.File;

import static com.sk89q.worldedit.bukkit.BukkitAdapter.adapt;
import static com.sk89q.worldedit.bukkit.BukkitAdapter.asBlockVector;

public class PasteHelper {
    private Location minimum, maximum;
    private Region newRegion;

    public void setMinimum(Location minimum) {
        this.minimum = minimum;
    }

    public Location getMinimum() {
        return minimum;
    }

    public void setMaximum(Location maximum) {
        this.maximum = maximum;
    }

    public Location getMaximum() {
        return maximum;
    }

    private PastedMine create(File file, Location location) {
        return new PastedMine(location.toVector()).setLocation(location).setFile(file).create();
    }

    public PastedMine paste(File file, Location location) {
        PastedMine pastedMine = create(file, location);
        BlockVector3 to = asBlockVector(location);
        World world = adapt(location.getWorld());

        try (
                Clipboard clipboard = HookHandler.getHookHandler().get(WorldEditHook.PLUGIN_NAME, WorldEditHook.class)
                        .getWorldEditWorldWriter()
                        .getClipboard(file)
        ) {
            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
            Region region = clipboard.getRegion();

            BlockVector3 clipboardOffset = region.getMinimumPoint().subtract(clipboard.getOrigin());
            Vector3 realTo = to.toVector3().add(clipboardHolder.getTransform().apply(clipboardOffset.toVector3()));
            Vector3 max = realTo.add(clipboardHolder.getTransform()
                    .apply(region.getMaximumPoint().subtract(region.getMinimumPoint()).toVector3()));

            newRegion = new CuboidRegionSelector(world, realTo.toBlockPoint(), max.toBlockPoint()).getRegion();

            setMinimum(adapt(adapt(world), newRegion.getMinimumPoint()));
            setMaximum(adapt(adapt(world), newRegion.getMaximumPoint()));
            return pastedMine;
        }
    }
}