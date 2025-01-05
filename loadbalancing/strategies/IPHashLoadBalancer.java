package strategies;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IPHashLoadBalancer {
    private final List<String> servers; // List of backend server addresses
    private final Map<String, String> ipToServerMap; // Map for session stickiness

    public IPHashLoadBalancer(List<String> servers) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("Server list cannot be null or empty.");
        }
        this.servers = servers;
        this.ipToServerMap = new HashMap<>();
    }

    /**
     * Get the server assigned to the client's IP.
     *
     * @param clientIp Client's IP address
     * @return Server address
     */
    public String getServer(String clientIp) {
        if (clientIp == null || clientIp.isEmpty()) {
            throw new IllegalArgumentException("Client IP cannot be null or empty.");
        }

        // Check if the IP is already assigned to a server for stickiness
        if (ipToServerMap.containsKey(clientIp)) {
            return ipToServerMap.get(clientIp);
        }

        // Calculate hash and assign server
        int serverIndex = Math.abs(clientIp.hashCode() % servers.size());
        String assignedServer = servers.get(serverIndex);

        // Store mapping for stickiness
        ipToServerMap.put(clientIp, assignedServer);
        return assignedServer;
    }

    public static void main(String[] args) {
        List<String> backendServers = List.of("192.168.1.1", "192.168.1.2", "192.168.1.3");

        IPHashLoadBalancer loadBalancer = new IPHashLoadBalancer(backendServers);

        // Simulate requests from different clients
        String clientIp1 = "192.168.100.1";
        String clientIp2 = "192.168.100.2";

        System.out.println("Server for " + clientIp1 + ": " + loadBalancer.getServer(clientIp1));
        System.out.println("Server for " + clientIp2 + ": " + loadBalancer.getServer(clientIp2));

        // Simulate repeat request from the same client
        System.out.println("Server for " + clientIp1 + " (repeat): " + loadBalancer.getServer(clientIp1));
    }
}
