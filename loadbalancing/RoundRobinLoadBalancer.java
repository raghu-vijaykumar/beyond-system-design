import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer {

    // List of backend servers
    private final List<String> servers;

    // Atomic counter for round-robin strategy
    private final AtomicInteger counter = new AtomicInteger(0);

    public RoundRobinLoadBalancer(List<String> servers) {
        this.servers = servers;
    }

    // Get the next server using round-robin
    public String getNextServer() {
        int index = counter.getAndIncrement() % servers.size();
        return servers.get(index);
    }

    public static void main(String[] args) {
        // List of backend servers (mocked as URLs or IP addresses)
        List<String> backendServers = List.of("http://server1.com", "http://server2.com", "http://server3.com");

        // Initialize the load balancer
        RoundRobinLoadBalancer loadBalancer = new RoundRobinLoadBalancer(backendServers);

        // Simulate client requests
        for (int i = 0; i < 10; i++) {
            String server = loadBalancer.getNextServer();
            System.out.println("Forwarding request " + (i + 1) + " to: " + server);
        }
    }
}
