# Load Balancing

Load balancing is a technique to distribute traffic across multiple servers to ensure that no single server becomes a bottleneck, improving availability, scalability, and fault tolerance.

## Conceptual Steps

1. Define Backends: Represent the servers or services to which the requests will be distributed.
2. Load Balancing Strategy: Implement a strategy such as:
   - Round Robin: Distribute requests sequentially.
   - Weighted: Favor certain servers based on weights.
   - Least Connections: Forward to the server with the fewest active connections.
3. Routing Requests: Forward client requests to the selected backend based on the strategy.
4. Fault Tolerance: Handle server failures gracefully.


