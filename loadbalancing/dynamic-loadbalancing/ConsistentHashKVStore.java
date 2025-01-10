import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashKVStore {
    public static class Cache {
        private final HashMap<Integer, String> cache;

        public Cache() {
            this.cache = new HashMap<>();
        }

        public void put(Integer key, String value) {
            cache.put(key, value);
        }

        public String get(Integer key) {
            return cache.get(key);
        }

        public String remove(Integer key) {
            return cache.remove(key);
        }
    }

    // Lets create cache ring with max 10 nodes
    private final int MAX_NODES = 10;
    // The key represents the range of the cache node, lets say max nodes is 10,
    // then the range is 0-10
    // if we add a new cache node, we need to rehash the key to the new range
    private final TreeMap<Integer, Cache> cacheRing;
    private final int replicationFactor;

    public ConsistentHashKVStore(int replicationFactor) {
        this.cacheRing = new TreeMap<>();
        this.replicationFactor = replicationFactor;
        for (int i = 0; i < replicationFactor + 1; i++) {
            addCache(new Cache());
        }
        System.out.println("Initial Cache Ring: " + cacheRing);
    }

    private void addCache(Cache cache) {
        if (cacheRing.size() >= MAX_NODES) {
            throw new IllegalArgumentException("Cache ring is full, No more caches can be added");
        }
        int midPointOfLargestGap = computeCacheHash();
        cacheRing.put(midPointOfLargestGap, cache);
        System.out.println("Added KVStore at " + midPointOfLargestGap + " Current Cache Ring: " + cacheRing);
    }

    private int computeCacheHash() {
        List<Integer> keys = new ArrayList<>(cacheRing.keySet());
        keys.add(0, -1);
        keys.add(MAX_NODES);
        int largestGap = 0;
        int midPoint = 0;
        for (int i = 0; i < keys.size() - 1; i++) {
            int gap = keys.get(i + 1) - keys.get(i);
            if (gap > largestGap) {
                largestGap = gap;
                midPoint = (keys.get(i) + keys.get(i + 1)) / 2;
            }
        }
        return midPoint;
    }

    public void put(Integer key, String value) {
        int[] cacheNodes = getCacheNodes(key);
        for (int cachedNode : cacheNodes) {
            cacheRing.get(cachedNode).put(key, value);
        }
        System.out.println("Storing  " + key + " in Cache: " + Arrays.toString(cacheNodes));
    }

    public int[] getCacheNodes(Integer key) {
        int keyHash = computeKeyHash(key);
        SortedMap<Integer, Cache> tailMap = cacheRing.tailMap(keyHash);
        int[] cacheNodes = new int[replicationFactor + 1];
        int i = 0;
        for (Map.Entry<Integer, Cache> entry : tailMap.entrySet()) {
            cacheNodes[i++] = entry.getKey();
            if (i == replicationFactor) {
                break;
            }
        }

        // if we have less than replicationFactor nodes, we need to add more nodes
        // from the beginning of the cache ring
        Iterator<Map.Entry<Integer, Cache>> iterator = cacheRing.entrySet().iterator();
        for (int j = i; j < replicationFactor + 1; j++) {
            cacheNodes[j] = iterator.next().getKey();
        }
        return cacheNodes;
    }

    public String get(Integer key) {
        if (cacheRing.isEmpty()) {
            return null;
        }

        int keyHash = computeKeyHash(key);
        Map.Entry<Integer, Cache> entry = cacheRing.ceilingEntry(keyHash) == null ? cacheRing.firstEntry()
                : cacheRing.ceilingEntry(keyHash);
        return entry.getValue().get(key);
    }

    private int computeKeyHash(Integer key) {
        return key % MAX_NODES;
    }

    private void removeCache() {
        cacheRing.remove(cacheRing.lastKey());
    }

    public static void main(String[] args) {
        ConsistentHashKVStore kvStore = new ConsistentHashKVStore(1);

        for (int i = 0; i < 100; i++) {
            kvStore.put(i, "value" + i);
        }
        kvStore.removeCache();
        System.out.println("After removing cache");
        for (int i = 0; i < 100; i++) {
            System.out.println(kvStore.get(i));
        }
        kvStore.addCache(new Cache());
        System.out.println("After adding cache");
        for (int i = 0; i < 100; i++) {
            System.out.println(kvStore.get(i));
        }
    }
}
