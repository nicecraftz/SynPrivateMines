package me.untouchedodin0.privatemines.utils;

public class QueueUtils {

//    private static final PrivateMines PRIVATE_MINES = PrivateMines.getInstance();
//    private static final MineService MINE_SERVICE = PRIVATE_MINES.getMineService();
//
//    public Queue<UUID> queue = new LinkedList<>();
//    public List<UUID> waitingInQueue = new ArrayList<>();
//
//    public void add(UUID uuid) {
//        if (!queue.contains(uuid)) {
//            queue.add(uuid);
//        }
//    }
//
//    public void claim(UUID uuid) {
//        if (waitingInQueue.contains(uuid)) return;
//        add(uuid);
//    }
//
//
//    public boolean isInQueue(UUID uuid) {
//        return waitingInQueue.contains(uuid);
//    }
//
//    public void claim(Player player) {
//        String mineRegionName = String.format("mine-%s", player.getUniqueId());
//        String fullRegionName = String.format("full-mine-%s", player.getUniqueId());
//        PregenStorage pregenStorage = PRIVATE_MINES.getPregenStorage();
//
//        if (MINE_SERVICE.has(player.getUniqueId())) {
//            player.sendMessage(ChatColor.RED + "You already own a mine!");
//            return;
//        }
//
//        if (queue.contains(player.getUniqueId())) {
//            player.sendMessage(ChatColor.RED + "You're already in the queue!");
//        }
//
//        claim(player.getUniqueId());
//
//        Task.syncRepeating(() -> {
//            AtomicInteger slot = new AtomicInteger(1);
//            List<UUID> uuidList = queue.stream().toList();
//
//            for (UUID uuid : uuidList) {
//                if (!uuid.equals(player.getUniqueId())) {
//                    slot.incrementAndGet();
//                } else {
//                    AtomicInteger place = new AtomicInteger(1);
//                    for (UUID uuid1 : uuidList) {
//                        if (!uuid1.equals(player.getUniqueId())) {
//                            place.incrementAndGet();
//                        }
//                    }
//                    int estimateSeconds = place.get() * 3;
//
////          player.sendTitle(ChatColor.GREEN + "You're at slot #" + slot.get(),
////              ChatColor.YELLOW + String.format(" Estimated wait time: %d seconds!",
////                  estimateSeconds));
//                }
//            }
//        }, 0L, 60L);
//
//        Task.syncRepeating(() -> {
//            UUID poll = queue.poll();
//            if (poll == null) {
//                return;
//            }
//            if (poll.equals(player.getUniqueId())) {
//                player.sendMessage(ChatColor.GREEN + "Creating your mine.....");
//
//                PregenMine pregenMine = pregenStorage.getAndRemove();
//                MineType mineType = mineTypeRegistry.getDefault();
//                Location location = pregenMine.getLocation();
//                Location spawn = pregenMine.getSpawnLocation();
//                Location corner1 = pregenMine.getLowerRails();
//                Location corner2 = pregenMine.getUpperRails();
//                Location minimum = pregenMine.getFullMin();
//                Location maximum = pregenMine.getFullMax();
//                BlockVector3 miningRegionMin = BukkitAdapter.asBlockVector(Objects.requireNonNull(corner1));
//                BlockVector3 miningRegionMax = BukkitAdapter.asBlockVector(Objects.requireNonNull(corner2));
//                BlockVector3 fullRegionMin = BukkitAdapter.asBlockVector(Objects.requireNonNull(minimum));
//                BlockVector3 fullRegionMax = BukkitAdapter.asBlockVector(Objects.requireNonNull(maximum));
//
//                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
//                RegionManager regionManager = container.get(BukkitAdapter.adapt(Objects.requireNonNull(spawn)
//                        .getWorld()));
//
//                ProtectedCuboidRegion miningRegion = new ProtectedCuboidRegion(
//                        mineRegionName,
//                        miningRegionMin,
//                        miningRegionMax
//                );
//                ProtectedCuboidRegion fullRegion = new ProtectedCuboidRegion(
//                        fullRegionName,
//                        fullRegionMin,
//                        fullRegionMax
//                );
//
//                if (regionManager != null) {
//                    regionManager.addRegion(miningRegion);
//                    regionManager.addRegion(fullRegion);
//                }
//
//                Mine mine = new Mine(PRIVATE_MINES);
//                MineData mineData = new MineData(
//                        player.getUniqueId(),
//                        corner2,
//                        corner1,
//                        minimum,
//                        maximum,
//                        Objects.requireNonNull(location),
//                        spawn,
//                        mineType,
//                        false,
//                        5.0
//                );
//                mine.setMineData(mineData);
//                SQLUtils.claim(location);
//                SQLUtils.insert(mine);
//
//                mineStorage.addMine(player.getUniqueId(), mine);
//
//                Task.syncDelayed(() -> spawn.getBlock().setType(Material.AIR, false));
//                pregenMine.teleport(player);
//                mine.handleReset();
//            }
//        }, 0L, 120L);
//    }
}
