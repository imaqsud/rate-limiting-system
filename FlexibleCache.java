import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

interface Storage<K, V> {
    V get(K key);
    void put(K key, V value);
    void remove(K key);
    boolean containsKey(K key);
    int size();
}

class SimpleStorage<K, V> implements Storage<K, V> {
    private final Map<K, V> map = new HashMap<>();

    @Override
    public V get(K key) { return map.get(key); }

    @Override
    public void put(K key, V value) { map.put(key, value); }

    @Override
    public void remove(K key) { map.remove(key); }

    @Override
    public boolean containsKey(K key) { return map.containsKey(key); }

    @Override
    public int size() { return map.size(); }
}

interface EvictionPolicy<K> {
    void keyAccessed(K key);
    void keyAdded(K key);
    K evictKey(); // Returns the key that should be removed
}

class LRUEvictionPolicy<K> implements EvictionPolicy<K> {
    // LinkedHashMap with accessOrder=true tracks MRU status automatically
    private final Map<K, Boolean> lruOrderMap = new LinkedHashMap<>(16, 0.75f, true);

    @Override
    public void keyAccessed(K key) { lruOrderMap.get(key); } // Moves key to end/MRU position

    @Override
    public void keyAdded(K key) { lruOrderMap.put(key, true); }

    @Override
    public K evictKey() {
        // The first entry in an access-ordered LinkedHashMap is the LRU item
        for (K key : lruOrderMap.keySet()) {
            return key;
        }
        return null;
    }
}

enum EvictionAlgorithm {
    LRU,
    LFU,
    RANK_BASED,
    RANK_BASED_LRU
}

public class FlexibleCache<K, V> {
    private final Storage<K, V> storage;
    private final EvictionPolicy<K> evictionPolicy;
    private final int capacity;

    public FlexibleCache(int capacity, EvictionAlgorithm evictionAlgorithm) {
        this.capacity = capacity;
        if (Objects.requireNonNull(evictionAlgorithm) == EvictionAlgorithm.LRU) {
            this.evictionPolicy = new LRUEvictionPolicy<>();
        } else {
            throw new RuntimeException("Eviction algorithm not supported.");
        }
        this.storage = new SimpleStorage<>();
    }

    public V get(K key) {
        V value = storage.get(key);
        if (value != null) {
            evictionPolicy.keyAccessed(key);
        }
        return value;
    }

    public void put(K key, V value) {
        if (storage.containsKey(key)) {
            storage.put(key, value);
            evictionPolicy.keyAccessed(key);
            return;
        }

        if (storage.size() >= capacity) {
            K keyToEvict = evictionPolicy.evictKey();
            if (keyToEvict != null) {
                storage.remove(keyToEvict);
                // keyRemoved is called by the policy itself in the full LFU implementation above
                System.out.println("Evicted key: " + keyToEvict);
            }
        }
        storage.put(key, value);
        evictionPolicy.keyAdded(key);
    }
}
