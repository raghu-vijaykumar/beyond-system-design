package strategies;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashLoadBalancer {
    private final NavigableMap<Integer, String> hashRing = new TreeMap<>();
    private final int numberOfReplicas;

    /**
     * Constructor for the load balancer.
     *
     * @param numberOfReplicas Number of virtual nodes (replicas) per server
     */
    public ConsistentHashLoadBalancer(int numberOfReplicas) {
        if (numberOfReplicas <= 0) {
            throw new IllegalArgumentException("Number of replicas must be positive.");
        }
        this.numberOfReplicas = numberOfReplicas;
    }

    /**
     * Adds a server to the hash ring.
     *
     * @param server Server identifier (e.g., IP address or name)
     */
    public void addServer(String server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            int hash = getHash(server + "#" + i);
            hashRing.put(hash, server);
        }
    }

    /**
     * Removes a server from the hash ring.
     *
     * @param server Server identifier to remove
     */
    public void removeServer(String server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            int hash = getHash(server + "#" + i);
            hashRing.remove(hash);
        }
    }

    /**
     * Finds the server for a given key.
     *
     * @param key The key to be mapped to a server (e.g., client IP or request
     *            identifier)
     * @return Server responsible for the key
     */
    public String getServer(String key) {
        if (hashRing.isEmpty()) {
            throw new IllegalStateException("No servers available in the hash ring.");
        }

        int hash = getHash(key);
        System.out.println("Hash for " + key + ": " + hash);
        SortedMap<Integer, String> tailMap = hashRing.tailMap(hash);
        System.out.println("Tail map: " + tailMap);
        int targetHash = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
        System.out.println("Target hash: " + targetHash);
        return hashRing.get(targetHash);
    }

    /**
     * Simple hash function to compute the hash for a given input.
     *
     * @param key Input string
     * @return Hash value
     */
    private int getHash(String key) {
        return Math.abs(key.hashCode());
    }

    public static void main(String[] args) {
        ConsistentHashLoadBalancer loadBalancer = new ConsistentHashLoadBalancer(3);

        // Add servers
        loadBalancer.addServer("192.168.1.1");
        loadBalancer.addServer("192.168.1.2");
        loadBalancer.addServer("192.168.1.3");
        loadBalancer.addServer("192.168.1.4");
        loadBalancer.addServer("192.168.1.5");

        // Map keys to servers
        String[] clientIps = { "192.161.100.1", "192.160.100.2", "1.168.100.3", "12.168.100.4" };

        for (String clientIp : clientIps) {
            System.out.println("Server for " + clientIp + ": " + loadBalancer.getServer(clientIp));
        }

        // Remove a server
        System.out.println("\nRemoving server 192.168.1.3...");
        loadBalancer.removeServer("192.168.1.3");

        for (String clientIp : clientIps) {
            System.out.println("Server for " + clientIp + ": " + loadBalancer.getServer(clientIp));
        }
    }
}
