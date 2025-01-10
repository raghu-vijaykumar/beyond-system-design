# Building a Simple Consistent Hash Key-Value Store in Java

Consistent hashing is a widely-used technique in distributed systems to distribute data across nodes while minimizing data movement during node addition or removal. This article walks through the implementation of a consistent hash key-value store in Java, focusing on its components, design considerations, and potential improvements.

---

## Overview of Consistent Hashing

In traditional hashing, a change in the number of nodes (e.g., servers) often results in rehashing most keys. Consistent hashing addresses this by ensuring that only a subset of keys needs to be reassigned when nodes are added or removed. This is achieved by mapping nodes and keys to a circular hash space.

---

## Key Components of the Implementation

The implementation in Java comprises the following components:

1. **Cache:** A simple key-value store using a `HashMap`.
2. **Cache Ring:** A `TreeMap` to represent the sorted ring of cache nodes.
3. **Replication Factor:** Defines the number of replicas for each key for fault tolerance.

### Data Structures Used

- **`HashMap`:** Used in the `Cache` class to store key-value pairs.
- **`TreeMap`:** Used for the cache ring to maintain nodes in sorted order for efficient lookups and rebalancing.

### Code Structure

Below, we provide a step-by-step explanation of the code:

---

## Step 1: The Cache Class

The `Cache` class provides basic operations like `put`, `get`, and `remove`.

```java
public static class Cache {
    public final HashMap<Integer, String> cache;

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
```

## Step 2: Adding Cache Nodes to the Ring

Caches are added to the TreeMap, which ensures that nodes remain sorted in hash order. A new node's position is determined based on the largest gap in the current ring.

```java
private void addCache(Cache cache) {
    int currCacheKey = computeCacheHash();
    cacheRing.put(currCacheKey, cache);
    System.out.println("Added KVStore at " + currCacheKey);
}
```

The hash for the new cache is computed as the midpoint of the largest gap between existing keys:

```java
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
```

## Step 3: Storing Data in the Cache

Data is stored in multiple nodes based on the replication factor:

```java
public void put(Integer key, String value) {
    int[] cacheNodes = getCacheNodes(key);
    for (int cachedNode : cacheNodes) {
        cacheRing.get(cachedNode).put(key, value);
    }
}
```

The target nodes are determined using the key's hash value and the sorted structure of TreeMap:

```java
public int[] getCacheNodes(Integer key) {
    int keyHash = computeKeyHash(key);
    SortedMap<Integer, Cache> tailMap = cacheRing.tailMap(keyHash);

    int[] cacheNodes = new int[replicationFactor + 1];
    Iterator<Map.Entry<Integer, Cache>> iterator = cacheRing.entrySet().iterator();

    int i = 0;
    for (Map.Entry<Integer, Cache> entry : tailMap.entrySet()) {
        cacheNodes[i++] = entry.getKey();
        if (i == replicationFactor + 1) break;
    }

    for (int j = i; j < replicationFactor + 1; j++) {
        cacheNodes[j] = iterator.next().getKey();
    }
    return cacheNodes;
}
```

## Step 4: Retrieving Data from the Cache

To retrieve data, the algorithm determines the appropriate cache node for the key:

```java
public String get(Integer key) {
    if (cacheRing.isEmpty()) {
        return null;
    }

    int keyHash = computeKeyHash(key);
    Map.Entry<Integer, Cache> entry = cacheRing.ceilingEntry(keyHash) == null ? cacheRing.firstEntry()
            : cacheRing.ceilingEntry(keyHash);
    return entry.getValue().get(key);
}
```

## Step 5: Rebalancing the Cache Ring

When a cache node is added or removed, the keys need to be redistributed. This is done by moving keys to/from adjacent nodes.

## Improvements and Considerations

- Multithreading

  - Current implementation is not thread-safe. Use synchronization or a thread-safe structure like ConcurrentSkipListMap for cacheRing.
  - Avoid contention by using locks at a finer granularity (e.g., per cache).

- Handling Hot Caches (nodes with disproportionately high traffic) can be alleviated by:

  - Using virtual nodes: Assign multiple positions to a single cache in the hash ring.
  - Load balancing strategies like random sampling.

- Minimizing Data Rebalancing

  - Use algorithms like Jump Consistent Hashing to reduce movement when adding/removing nodes.
  - Prioritize data locality by assigning ranges to nodes instead of hashing keys.

- Fault Tolerance: Replication ensures resilience to node failures. Monitor and redistribute keys during node failures.

- Scalability: To support large-scale systems, integrate a distributed caching solution like Redis or Memcached.

- Hashing Algorithm: The current modulo-based hash is simple but can result in collisions. Use cryptographic hashes (e.g., MD5, SHA-256) for better distribution.

## Conclusion

This implementation of consistent hashing demonstrates the fundamental principles and challenges of distributed key-value stores. By improving multithreading, addressing hot caches, and minimizing data rebalancing, this design can be extended to real-world distributed systems.
