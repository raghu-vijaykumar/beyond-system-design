import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashKVStore {

    // Inner class representing a simple cache with basic operations
    public static class Cache {
        public final HashMap<Integer, String> cache;

        public Cache() {
            this.cache = new HashMap<>();
        }

        // Store a key-value pair in the cache
        public void put(Integer key, String value) {
            cache.put(key, value);
        }

        // Retrieve a value from the cache by key
        public String get(Integer key) {
            return cache.get(key);
        }

        // Remove a key-value pair from the cache
        public String remove(Integer key) {
            return cache.remove(key);
        }
    }

    // Maximum number of nodes allowed in the cache ring
    private final int MAX_NODES = 10;
    // TreeMap to maintain the cache nodes in a sorted order
    private final TreeMap<Integer, Cache> cacheRing;
    // Number of replicas for each key
    private final int replicationFactor;

    // Constructor to initialize the cache ring with a given replication factor
    public ConsistentHashKVStore(int replicationFactor) {
        this.cacheRing = new TreeMap<>();
        if (replicationFactor > MAX_NODES - 1) {
            throw new IllegalArgumentException("Replication factor cannot be greater than the number of nodes");
        }
        this.replicationFactor = replicationFactor;
        for (int i = 0; i <= replicationFactor; i++) {
            addCache(new Cache());
        }
        System.out.println("Initial Cache Ring: " + cacheRing);
    }

    // Add a new cache node to the cache ring
    private void addCache(Cache cache) {
        if (cacheRing.size() >= MAX_NODES) {
            throw new IllegalArgumentException("Cache ring is full, No more caches can be added");
        }
        int currCacheKey = computeCacheHash();
        cacheRing.put(currCacheKey, cache);
        System.out.println("Added KVStore at " + currCacheKey + " Current Cache Ring: " + cacheRing);

        // Rebalance the cache ring by moving cached items to the newly added cache node
        if (cacheRing.size() > 1) {
            int nextCacheKey = cacheRing.higherKey(currCacheKey) == null ? cacheRing.firstEntry().getKey()
                    : cacheRing.higherKey(currCacheKey);
            Cache nextCache = cacheRing.get(nextCacheKey);

            Cache currCache = cacheRing.get(currCacheKey);
            for (Map.Entry<Integer, String> e : nextCache.cache.entrySet()) {
                if (e.getKey() <= currCacheKey) {
                    currCache.put(e.getKey(), e.getValue());
                    if (replicationFactor < 1) {
                        nextCache.remove(e.getKey());
                    }
                }
            }
        }
        System.out.println("Rebalanced Cache Ring after adding new cache node");
    }

    // Compute the hash for a new cache node based on the largest gap in the current
    // keys
    private int computeCacheHash() {
        // Convert the set of keys from the cache ring into a list for easier
        // manipulation
        List<Integer> keys = new ArrayList<>(cacheRing.keySet());
        keys.add(0, -1);
        keys.add(MAX_NODES);
        int largestGap = 0;

        // Initialize a variable to store the midpoint of the largest gap
        int midPoint = 0;

        // Iterate through the list of keys to find the largest gap
        for (int i = 0; i < keys.size() - 1; i++) {
            // Calculate the gap between the current key and the next key
            int gap = keys.get(i + 1) - keys.get(i);

            // If the current gap is larger than the largest recorded gap, update the
            // largest gap
            // and calculate the midpoint of this gap
            if (gap > largestGap) {
                largestGap = gap;
                midPoint = (keys.get(i) + keys.get(i + 1)) / 2;
            }
        }

        // Return the midpoint of the largest gap as the hash for the new cache node
        return midPoint;
    }

    // Store a key-value pair in the appropriate cache nodes
    public void put(Integer key, String value) {
        int[] cacheNodes = getCacheNodes(key);
        for (int cachedNode : cacheNodes) {
            cacheRing.get(cachedNode).put(key, value);
        }
        System.out.println("Storing " + key + " in Cache: " + Arrays.toString(cacheNodes));
    }

    // Determine which cache nodes should store a given key
    public int[] getCacheNodes(Integer key) {
        int keyHash = computeKeyHash(key);
        SortedMap<Integer, Cache> tailMap = cacheRing.tailMap(keyHash);
        int[] cacheNodes = new int[replicationFactor + 1];
        int i = 0;
        for (Map.Entry<Integer, Cache> entry : tailMap.entrySet()) {
            cacheNodes[i++] = entry.getKey();
            if (i == replicationFactor + 1) {
                break;
            }
        }

        // If we have less than replicationFactor nodes, add more nodes from the
        // beginning of the cache ring
        Iterator<Map.Entry<Integer, Cache>> iterator = cacheRing.entrySet().iterator();
        for (int j = i; j < replicationFactor + 1; j++) {
            cacheNodes[j] = iterator.next().getKey();
        }
        return cacheNodes;
    }

    // Retrieve a value from the cache using a key
    public String get(Integer key) {
        if (cacheRing.isEmpty()) {
            return null;
        }

        int keyHash = computeKeyHash(key);
        Map.Entry<Integer, Cache> entry = cacheRing.ceilingEntry(keyHash) == null ? cacheRing.firstEntry()
                : cacheRing.ceilingEntry(keyHash);
        return entry.getValue().get(key);
    }

    // Compute the hash for a key to determine its position in the cache ring
    private int computeKeyHash(Integer key) {
        return key % MAX_NODES;
    }

    // Remove the last cache node from the cache ring
    private void removeCache() {
        Integer lastKey = cacheRing.lastKey();
        if (lastKey == null) {
            throw new IllegalArgumentException("Cache ring is empty, No more caches can be removed");
        }
        cacheRing.remove(lastKey);
        System.out.println("Removed KVStore at " + lastKey + " Current Cache Ring: " + cacheRing);
    }

    // Main method to demonstrate the functionality of the ConsistentHashKVStore
    public static void main(String[] args) {
        ConsistentHashKVStore kvStore = new ConsistentHashKVStore(2);

        // Add key-value pairs to the store
        for (int i = 0; i < 10; i++) {
            kvStore.put(i, "value" + i);
        }

        // Remove a cache node and print the values to demonstrate rebalancing
        kvStore.removeCache();
        for (int i = 0; i < 10; i++) {
            System.out.println(kvStore.get(i));
        }

        // Add more cache nodes and print the values to demonstrate rebalancing
        kvStore.addCache(new Cache());
        kvStore.addCache(new Cache());
        kvStore.addCache(new Cache());
        for (int i = 0; i < 10; i++) {
            System.out.println(kvStore.get(i));
        }
    }
}
