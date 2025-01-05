package strategies;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class Server {
    private final String name;
    private final AtomicInteger activeConnections;

    public Server(String name) {
        this.name = name;
        this.activeConnections = new AtomicInteger(0);
    }

    public String getName() {
        return name;
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }

    public void incrementConnections() {
        activeConnections.incrementAndGet();
    }

    public void decrementConnections() {
        activeConnections.decrementAndGet();
    }

    @Override
    public String toString() {
        return name + " (Active Connections: " + activeConnections + ")";
    }
}

class LeastConnectionsLoadBalancer {
    private final List<Server> servers;

    public LeastConnectionsLoadBalancer(List<Server> servers) {
        this.servers = servers;
    }

    // Get the server with the least active connections
    public Server getNextServer() {
        return servers.stream()
                .min(Comparator.comparingInt(Server::getActiveConnections))
                .orElseThrow(() -> new RuntimeException("No servers available"));
    }

    public void simulateRequest(Server server) {
        // Simulate handling a request by incrementing and then decrementing connections
        server.incrementConnections();
        System.out.println("Forwarding request to: " + server);
        try {
            Thread.sleep((long) (Math.random() * 1000)); // Simulate request processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        server.decrementConnections();
    }

    public static void main(String[] args) {
        // Initialize servers
        List<Server> servers = List.of(
                new Server("http://server1.com"),
                new Server("http://server2.com"),
                new Server("http://server3.com"));

        // Initialize load balancer
        LeastConnectionsLoadBalancer loadBalancer = new LeastConnectionsLoadBalancer(servers);

        // Simulate client requests
        for (int i = 0; i < 20; i++) {
            Server server = loadBalancer.getNextServer();
            new Thread(() -> loadBalancer.simulateRequest(server)).start();
        }

    }
}
