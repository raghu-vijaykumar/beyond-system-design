import java.util.ArrayList;
import java.util.List;

public class WeightedLoadBalancer {

    private final List<String> servers; // List of servers
    private final List<Integer> weights; // List of server weights
    private final List<Integer> currentWeights; // Current weights for each server
    private int totalWeight; // Sum of all weights
    private int lastServerIndex = -1; // Index of the last selected server

    public WeightedLoadBalancer(List<String> servers, List<Integer> weights) {
        if (servers.size() != weights.size()) {
            throw new IllegalArgumentException("Servers and weights must have the same size.");
        }
        this.servers = servers;
        this.weights = weights;
        this.currentWeights = new ArrayList<>(weights.size());

        // Initialize current weights and calculate total weight
        totalWeight = 0;
        for (int weight : weights) {
            currentWeights.add(0);
            totalWeight += weight;
        }
    }

    // Get the next server using weighted round-robin logic
    public String getNextServer() {
        while (true) {
            lastServerIndex = (lastServerIndex + 1) % servers.size();

            // Increment the current weight of the selected server
            currentWeights.set(lastServerIndex,
                    currentWeights.get(lastServerIndex) + weights.get(lastServerIndex));
            System.out.println("Server: " + servers.get(lastServerIndex) + " Current weight: "
                    + currentWeights.get(lastServerIndex));
            // Check if this server should handle the request
            if (currentWeights.get(lastServerIndex) >= totalWeight) {
                currentWeights.set(lastServerIndex, currentWeights.get(lastServerIndex) - totalWeight);
                System.out.println("Selected server: " + servers.get(lastServerIndex) + " with current weight "
                        + currentWeights.get(lastServerIndex));
                return servers.get(lastServerIndex);
            }
        }
    }

    public static void main(String[] args) {
        // Define servers and their corresponding weights
        List<String> servers = List.of("http://server1.com", "http://server2.com", "http://server3.com");
        List<Integer> weights = List.of(5, 3, 2); // Weights: Server1=5, Server2=3, Server3=2

        // Initialize the weighted load balancer
        WeightedLoadBalancer loadBalancer = new WeightedLoadBalancer(servers, weights);

        // Simulate client requests
        for (int i = 0; i < 20; i++) {
            String server = loadBalancer.getNextServer();
            System.out.println("Forwarding request " + (i + 1) + " to: " + server);
        }
    }
}
