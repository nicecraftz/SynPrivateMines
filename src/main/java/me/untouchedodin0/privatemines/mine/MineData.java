package me.untouchedodin0.privatemines.mine;

import com.google.common.collect.ImmutableSet;
import me.untouchedodin0.privatemines.playershops.Shop;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MineData {
    private final Set<UUID> bannedPlayers = new HashSet<>();
    private final Set<UUID> friends = new HashSet<>();
    private final WeightedCollection<Material> materials = new WeightedCollection<>();

    private final UUID mineOwner;

    private MineStructure mineStructure;
    private MineType mineType;

    private boolean isOpen = false;
    private double tax = 5d;

    private Shop shop = null;

    private int maxPlayers = 0;
    private int maxMineSize = 0;

    public MineData(UUID mineOwner, MineStructure mineStructure, MineType mineType) {
        this.mineOwner = mineOwner;
        this.mineStructure = mineStructure;
        this.mineType = mineType;
    }

    public UUID getMineOwner() {
        return mineOwner;
    }

    public void setMineStructure(MineStructure mineStructure) {
        this.mineStructure = mineStructure;
    }

    public MineStructure getMineStructure() {
        return mineStructure;
    }

    public void setMineType(MineType mineType) {
        this.mineType = mineType;
    }

    public MineType getMineType() {
        return mineType;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        if (tax < 0) return;
        this.tax = tax;
    }

    public void removeBannedPlayer(UUID uuid) {
        bannedPlayers.remove(uuid);
    }

    public boolean isBanned(UUID uuid) {
        return bannedPlayers.contains(uuid);
    }

    public void addBannedPlayer(UUID uuid) {
        if (uuid.equals(mineOwner)) return;
        bannedPlayers.add(uuid);
    }

    public Set<UUID> getBannedPlayers() {
        return ImmutableSet.copyOf(bannedPlayers);
    }

    public void addFriend(UUID uuid) {
        if (uuid.equals(mineOwner)) return;
        friends.add(uuid);
    }

    public void removeFriend(UUID uuid) {
        friends.remove(uuid);
    }

    public Set<UUID> getFriends() {
        return ImmutableSet.copyOf(friends);
    }

    public WeightedCollection<Material> getMaterials() {
        return materials;
    }

    public boolean hasShop() {
        return shop != null;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        if (maxPlayers < 0) return;
        this.maxPlayers = maxPlayers;
    }

    public int getMaxMineSize() {
        return maxMineSize;
    }

    public void setMaxMineSize(int maxMineSize) {
        if (maxMineSize < 0) return;
        this.maxMineSize = maxMineSize;
    }
}
