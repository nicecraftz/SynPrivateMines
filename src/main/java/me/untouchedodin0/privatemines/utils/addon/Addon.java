package me.untouchedodin0.privatemines.utils.addon;

public abstract class Addon {

    public abstract void enable();

    public abstract void disable();

    public abstract void reload();

    public abstract AddonDescription addonDescription();


    public record AddonDescription(String name, String author, String version, String description) {
    }
}
