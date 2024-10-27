package me.untouchedodin0.privatemines.utils.addon;

import me.untouchedodin0.privatemines.PrivateMines;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class AddonManager {
    private final PrivateMines privateMines;
    private final Map<String, Addon> addons = new HashMap<>();

    public AddonManager(PrivateMines privateMines) {
        this.privateMines = privateMines;
    }

    @NotNull
    public CompletableFuture<@Nullable Class<? extends Addon>> findExpansionInFile(@NotNull final File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Class<? extends Addon> addonClass = FileUtil.findClass(file, Addon.class);
                if (addonClass != null) return addonClass;
                privateMines.logWarn(String.format("Failed to load addon %s, as it does not have a class which extends Addon",
                        file.getName()
                ));
                return null;
            } catch (VerifyError | NoClassDefFoundError e) {
                privateMines.logWarn(String.format("Failed to load addon %s, as it has a linkage error",
                        file.getName()
                ));
                return null;
            } catch (Exception e) {
                throw new CompletionException(e.getMessage() + " (addon file: " + file.getAbsolutePath() + ")", e);
            }
        });
    }

    public Optional<Addon> register(final CompletableFuture<@Nullable Class<? extends Addon>> clazz) {
        try {
            Optional<Addon> addonOptional = Optional.ofNullable(createAddonInstance(clazz));
            addonOptional.ifPresent(addon -> addons.put(addon.addonDescription().name().toLowerCase(), addon));
            return addonOptional;
        } catch (LinkageError | NullPointerException ex) {
            String reason = ex instanceof LinkageError ? " (Is a dependency missing?)" : " - One of its properties is null which is not allowed!";
            try {
                privateMines.getLogger()
                        .warning(String.format("Failed to load addon class %s %s",
                                clazz.get().getSimpleName(),
                                reason
                        ));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return Optional.empty();
    }

    public Addon createAddonInstance(final CompletableFuture<@Nullable Class<? extends Addon>> clazz) throws LinkageError {
        PrivateMines privateMines = PrivateMines.inst();

        try {
            return clazz.get().getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            if (ex.getCause() instanceof LinkageError) {
                throw (LinkageError) ex.getCause();
            }

            privateMines.getLogger().warning("There was an issue with loading an addon.");
            return null;
        }
    }

    public Map<String, Addon> getAddons() {
        return Map.copyOf(addons);
    }

    public Optional<Addon> get(String name) {
        return Optional.ofNullable(addons.get(name.toLowerCase()));
    }

    public Optional<Addon> remove(String name) {
        return Optional.ofNullable(addons.remove(name.toLowerCase()));
    }
}