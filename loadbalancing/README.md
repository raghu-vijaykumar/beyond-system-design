# Load Balancing

Load balancing is a technique to distribute traffic across multiple servers to ensure that no single server becomes a bottleneck, improving availability, scalability, and fault tolerance.

## Load Balancing Strategies

- Round Robin: Distribute requests sequentially.
- Weighted: Favor certain servers based on weights.
- Least Connections: Forward to the server with the fewest active connections.
- Least Response Time: Forward to the server with the shortest response time.
- IP Hash: Hash the client's IP address to determine which server to use.
- Consistent Hash: Use consistent hashing to distribute requests evenly.
- Geolocation: Forward to the server based on the client's geolocation.
- Adaptive Load: Combines multiple metrics (CPU usage, memory, response time, etc.) to decide the best server dynamically.
- Hybrid: Combines multiple strategies to balance load.

### Load Balancer Implementation

- Round Robin: [RoundRobinLoadBalancer.java](RoundRobinLoadBalancer.java)
- Weighted: [WeightedLoadBalancer.java](WeightedLoadBalancer.java)
- Least Connections: [LeastConnectionsLoadBalancer.java](LeastConnectionsLoadBalancer.java)
- Least Response Time: [LeastResponseTimeLoadBalancer.java](LeastResponseTimeLoadBalancer.java)
- IP Hash: [IPHashLoadBalancer.java](IPHashLoadBalancer.java)
- Consistent Hash: [ConsistentHashLoadBalancer.java](ConsistentHashLoadBalancer.java)
- Geolocation: [GeolocationLoadBalancer.java](GeolocationLoadBalancer.java)

## Real World Implementations

- Nginx: [Nginx Load Balancer](https://nginx.org/en/docs/http/load_balancing.html)
- HAProxy: [HAProxy Load Balancer](https://www.haproxy.com/blog/haproxy-load-balancer/)
- AWS ELB: [AWS Elastic Load Balancer](https://aws.amazon.com/elasticloadbalancing/)
- Kubernetes: [Kubernetes Load Balancer](https://kubernetes.io/docs/concepts/services-networking/service/)
- Spring Cloud LoadBalancer: [Spring Cloud LoadBalancer](https://spring.io/projects/spring-cloud-loadbalancer)
