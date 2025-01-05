package strategies;
import java.util.HashMap;
import java.util.Map;

public class GeolocationLoadBalancer {
    // Mapping of client region to server address
    private final Map<String, String> regionToServerMap = new HashMap<>();

    /**
     * Adds a server for a specific region.
     *
     * @param region       The region (e.g., "North America", "Europe")
     * @param serverAddress The server address (e.g., IP address or domain name)
     */
    public void addServer(String region, String serverAddress) {
        regionToServerMap.put(region.toLowerCase(), serverAddress);
    }

    /**
     * Removes a server for a specific region.
     *
     * @param region The region to remove
     */
    public void removeServer(String region) {
        regionToServerMap.remove(region.toLowerCase());
    }

    /**
     * Finds the server for a given client region.
     *
     * @param clientRegion The client's region
     * @return The server address for the region, or a default server if not found
     */
    public String getServer(String clientRegion) {
        String regionKey = clientRegion.toLowerCase();
        return regionToServerMap.getOrDefault(regionKey, "default-server.example.com");
    }

    public static void main(String[] args) {
        GeolocationLoadBalancer loadBalancer = new GeolocationLoadBalancer();

        // Add servers for different regions
        loadBalancer.addServer("North America", "na-server.example.com");
        loadBalancer.addServer("Europe", "eu-server.example.com");
        loadBalancer.addServer("Asia", "asia-server.example.com");

        // Simulate client requests from various regions
        String[] clientRegions = { "North America", "Europe", "Asia", "Africa" };

        for (String clientRegion : clientRegions) {
            System.out.println("Client region: " + clientRegion +
                    ", Routed to server: " + loadBalancer.getServer(clientRegion));
        }

        // Remove a server and test again
        System.out.println("\nRemoving server for Europe...");
        loadBalancer.removeServer("Europe");

        for (String clientRegion : clientRegions) {
            System.out.println("Client region: " + clientRegion +
                    ", Routed to server: " + loadBalancer.getServer(clientRegion));
        }
    }
}
