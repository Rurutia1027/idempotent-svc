## 📦 Idempotency Toolkit

A modular, pluggable **idempotency toolkit** for ensuring exactly-once execution across **HTTP APIs**, **message queues**, and **database operations**. Built with **Spring Boot**, **Redis**, and **AOP**, and supports monitoring, CI/CD, and containerized deployments.

---

### 🗂️ Project Structure

```
idempotent-toolkit/
├── idm-sdk/                     # SDK modules
│   ├── idm-facade/             # Public annotations and shared interfaces
│   └── idm-core/               # Core AOP logic and Redis-backed idempotency handler
│
├── idm-apps/                   # Use-case applications
│   ├── http/                  # REST-based idempotent APIs
│   ├── mq/                    # MQ consumers with idempotency (e.g. ActiveMQ)
│   └── db/                    # (Optional) DB-related use cases
│
├── idm-demo/                   # Combined local runnable example (HTTP + MQ)
│
├── deployment/                 # Deployment assets
│   ├── docker-compose/        # Local dev environment
│   ├── k8s/                   # Kubernetes manifests
│   └── terraform/             # Infrastructure provisioning
│
├── cicd/                       # CI/CD automation
│   ├── Jenkinsfile            # Jenkins pipeline
│   ├── github-actions/        # GitHub Actions workflows
│   └── scripts/               # Build/test/deploy scripts
│
├── monitoring/                 # Observability stack
│   ├── prometheus/           # Metrics collection
│   ├── grafana/              # Dashboards
│   └── alerting/             # Alertmanager configs
└── README.md
```

---

### ⚙️ Core Concepts

#### ✅ `@Idempotent` Annotation

Used to declare methods that require idempotency control. Supports multiple scenes and strategies:

```java
@Idempotent(
    key = "#request.orderId",
    type = IdempotentTypeEnum.SPEL,
    scene = IdempotentSceneEnum.HTTP
)
public void handleOrder(Request request) {
    // business logic
}
```

---

### 🔁 Execution Flow

1. Operation triggered (HTTP request, MQ message, etc.)
2. AOP intercepts method via `IdempotentAspect`
3. Redis key is generated from the method input (based on `@Idempotent.key`)
4. If key exists → reject/skip execution
5. If key absent → execute method and store key in Redis
6. Optional: auto-expire Redis key or remove it post-success

---

### 🧪 Integration Test Example (MQ)

```java
@Component
public class OrderConsumer {
    @Idempotent(scene = IdempotentSceneEnum.MQ, type = IdempotentTypeEnum.SPEL, key = "#payload.orderId")
    public void process(String payload) {
        // idempotent message handling
    }
}
```

```java
@SpringBootTest
@ActiveProfiles("test")
class ActiveMQIdempotentIT {
    @Autowired JmsTemplate jmsTemplate;
    @Autowired OrderConsumer consumer;

    @Test
    void testIdempotency() throws InterruptedException {
        String payload = "{\"orderId\":\"123\"}";
        jmsTemplate.convertAndSend("queue.idem.test", payload);
        jmsTemplate.convertAndSend("queue.idem.test", payload); // duplicated

        Thread.sleep(500);
        verify(consumer, times(1)).process(any());
    }
}
```

---

### 📊 Performance Benchmark

Using JMH:

```java
@BenchmarkMode(Mode.Throughput)
@State(Scope.Thread)
public class IdempotentBenchmark {
    private IdempotentExecuteHandler handler;

    @Setup public void setup() {
        handler = new RedisIdempotentExecuteHandler();
    }

    @Benchmark
    public void testExecute() throws Throwable {
        handler.execute(joinPoint, mock(Idempotent.class));
    }
}
```

---

### 🚀 CI/CD Pipeline

#### Jenkins Flow

1. Build all modules under `idm-sdk` and `idm-apps`
2. Run unit + integration tests
3. Build Docker images
4. Apply Terraform (AWS provisioning)
5. Deploy via Helm or `kubectl`

#### GitHub Actions *(optional)*

* Auto-test + build on PR
* Deploy snapshot builds to container registry

---

### 🐳 Local Testing

```bash
cd deployment/docker-compose
docker-compose up --build
```

Includes:

* Redis
* ActiveMQ
* HTTP + MQ apps

Accessible via:

* `localhost:8080` (HTTP)
* `localhost:8161` (ActiveMQ Console)

---

### 📈 Observability

```bash
kubectl apply -f deployment/k8s/
```

* **Grafana**: `http://<grafana-ip>:3000`
* **Prometheus** scrapes metrics from all `*idem-app` services
* Redis/MQ exposed via NodePort

