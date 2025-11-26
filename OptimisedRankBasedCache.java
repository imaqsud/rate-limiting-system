import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

// A class to hold cache item details (key, value, rank)
class OptimisedCacheEntry<K, V> {
    final K key;
    V value;
    Integer rank;

    OptimisedCacheEntry(K key, V value, int rank) {
        this.key = key;
        this.value = value;
        this.rank = rank;
    }
}

public class OptimisedRankBasedCache<K, V> {
    private final int capacity;
    private final Map<K, OptimisedCacheEntry<K, V>> cacheMap;
    // TreeMap orders entries by rank (Key = Rank, Value = Set of Keys with that Rank)
    private final TreeMap<Integer, Map<K, Boolean>> rankMap;

    public OptimisedRankBasedCache(int capacity) {
        this.capacity = capacity;
        this.cacheMap = new HashMap<>();
        // Use a TreeMap to track ranks efficiently (O(log N) for all operations)
        this.rankMap = new TreeMap<>();
    }

    // Get operation remains O(1)
    public V get(K key) {
        OptimisedCacheEntry<K, V> entry = cacheMap.get(key);
        return (entry == null) ? null : entry.value;
    }

    // Put operation is optimized to O(log N) overall
    public void put(K key, V value, int rank) {
        if (cacheMap.containsKey(key)) {
            // Optimization: Remove old rank entry from rankMap before modifying the object
            OptimisedCacheEntry<K, V> existingEntry = cacheMap.get(key);
            removeKeyFromRankMap(existingEntry.rank, existingEntry.key);

            // Update the data fields
            existingEntry.value = value;
            existingEntry.rank = rank;

            // Add to new rank list in rankMap (O(log N))
            addKeyToRankMap(rank, key);

        } else {
            // New entry: Check capacity and evict if necessary (O(log N))
            if (cacheMap.size() >= capacity) {
                evictLowestRanked();
            }

            // Create and add new entries (O(log N))
            OptimisedCacheEntry<K, V> newEntry = new OptimisedCacheEntry<>(key, value, rank);
            cacheMap.put(key, newEntry);
            addKeyToRankMap(rank, key);
        }
    }

    // --- Helper methods for TreeMap Management ---

    private void addKeyToRankMap(int rank, K key) {
        rankMap.computeIfAbsent(rank, k -> new HashMap<>()).put(key, true);
    }

    private void removeKeyFromRankMap(int rank, K key) {
        Map<K, Boolean> keysAtRank = rankMap.get(rank);
        if (keysAtRank != null) {
            keysAtRank.remove(key);
            if (keysAtRank.isEmpty()) {
                rankMap.remove(rank); // Clean up the rank entry if no items remain
            }
        }
    }

    // Evicts the lowest ranked item from the TreeMap and the main cacheMap
    private void evictLowestRanked() {
        if (rankMap.isEmpty()) return;

        // Get the lowest rank key (O(log N))
        Map.Entry<Integer, Map<K, Boolean>> lowestRankEntry = rankMap.firstEntry();
        int lowestRank = lowestRankEntry.getKey();
        Map<K, Boolean> keysAtLowestRank = lowestRankEntry.getValue();

        // Get the first key in that set to evict (uses iteration, but is O(1) as we only take one)
        Iterator<K> iterator = keysAtLowestRank.keySet().iterator();
        K keyToEvict = iterator.next();
        iterator.remove(); // Remove it from the rankMap's internal set

        if (keysAtLowestRank.isEmpty()) {
            rankMap.remove(lowestRank); // Clean up the empty rank entry
        }

        cacheMap.remove(keyToEvict); // Remove from main cacheMap (O(1))
    }
}
