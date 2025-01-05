package strategies;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LeastResponseTimeLoadBalancer {
    private final Map<String, Server> serverMap = new HashMap<>();

    /**
     * Represents a server with response time tracking.
     */
    static class Server {
        private final String address; // Server address
        private int totalResponseTime; // Total response time of completed requests
        private int requestCount; // Number of completed requests

        public Server(String address) {
            this.address = address;
            this.totalResponseTime = 0;
            this.requestCount = 0;
        }

        public String getAddress() {
            return address;
        }

        public synchronized void recordResponseTime(int responseTime) {
            this.totalResponseTime += responseTime;
            this.requestCount++;
        }

        public synchronized double getAverageResponseTime() {
            return requestCount == 0 ? 0 : (double) totalResponseTime / requestCount;
        }
    }

    /**
     * Adds a server to the load balancer.
     *
     * @param serverAddress Address of the server
     */
    public void addServer(String serverAddress) {
        serverMap.putIfAbsent(serverAddress, new Server(serverAddress));
    }

    /**
     * Removes a server from the load balancer.
     *
     * @param serverAddress Address of the server to remove
     */
    public void removeServer(String serverAddress) {
        serverMap.remove(serverAddress);
    }

    /**
     * Gets the server with the least average response time.
     *
     * @return The selected server's address
     */
    public String getServer() {
        Optional<Server> selectedServer = serverMap.values().stream()
                .min((s1, s2) -> Double.compare(s1.getAverageResponseTime(), s2.getAverageResponseTime()));

        return selectedServer.map(Server::getAddress)
                .orElseThrow(() -> new IllegalStateException("No servers available."));
    }

    /**
     * Records a response time for a specific server.
     *
     * @param serverAddress Address of the server
     * @param responseTime  Response time in milliseconds
     */
    public void recordResponseTime(String serverAddress, int responseTime) {
        Server server = serverMap.get(serverAddress);
        if (server == null) {
            throw new IllegalArgumentException("Server not found: " + serverAddress);
        }
        server.recordResponseTime(responseTime);
    }

    public static void main(String[] args) {
        LeastResponseTimeLoadBalancer loadBalancer = new LeastResponseTimeLoadBalancer();

        // Add servers
        loadBalancer.addServer("192.168.1.1");
        loadBalancer.addServer("192.168.1.2");
        loadBalancer.addServer("192.168.1.3");

        // Simulate response times
        loadBalancer.recordResponseTime("192.168.1.1", 120);
        loadBalancer.recordResponseTime("192.168.1.2", 100);
        loadBalancer.recordResponseTime("192.168.1.3", 150);

        System.out.println("Selected server: " + loadBalancer.getServer());

        // Simulate more response times
        loadBalancer.recordResponseTime("192.168.1.1", 80);
        loadBalancer.recordResponseTime("192.168.1.2", 90);
        loadBalancer.recordResponseTime("192.168.1.3", 110);

        System.out.println("Selected server: " + loadBalancer.getServer());

        // Remove a server
        loadBalancer.removeServer("192.168.1.2");

        System.out.println("Selected server after removal: " + loadBalancer.getServer());
    }
}
