# Idempotency Toolkit for HTTP, MQ, and DB Operations

This project provides a modular, pluggable idempotency toolkit that supports various operational modes such as HTTP
APIs, Message Queue consumers, and database-driven actions. It leverages Redis for idempotent state tracking and
includes integrated monitoring, deployment automation, and CI/CD pipelines.

## Project Structure Overview

```text
idem-toolkit/
├── idem-sdk/                   # Core SDK modules
│   ├── idem-svc-api/          # Public annotations, interfaces
│   └── idem-svc-core/         # AOP and Redis-based idempotency implementation
│
├── applications/              # Scenario-specific applications
│   ├── http-idem-app/         # REST API-based idempotent interface
│   ├── activemq-idem-app/     # ActiveMQ-based consumer with idempotency
│   ├── kafka-idem-app/        # (Optional) Kafka-based example
│   └── examples/              # Combined example for local development
│
├── deployment/                # Deployment configurations
│   ├── docker-compose/        # Local test stack
│   ├── k8s/                   # Kubernetes manifests
│   └── terraform/             # AWS infrastructure provisioning
│
├── cicd/                      # Jenkins pipeline and GitHub Actions
│   ├── Jenkinsfile            # Jenkins declarative pipeline
│   ├── github-actions/        # Optional GitHub Actions workflows
│   └── scripts/               # Build & deployment scripts
│
├── monitoring/                # Prometheus + Grafana dashboards
│   ├── prometheus/            # Prometheus config and scrape targets
│   ├── grafana/               # Dashboard JSON templates
│   └── alerting/              # Example Alertmanager rules
└── README.md
```

## Idempotency Workflow Design

### Goals

- Ensure exactly-once execution for distributed operations across HTTP, MQ, and Database interaction layers.
- Support reprocessing and deduplicate logic with retry and DLQ strategies.
- Centralize idempotent control with `@Idempotent` annotation and Redis-backed token store.

### General Architecture

![](todo)

## Core Components

- `@Idempotent`: Marks methods requiring idempotency control
- IdempotentAspect: AOP that wraps execution with pre/post hooks
- AbstractIdempotentExecuteHandler: Common handler interface for any protocol type
- RedisIdempotentStore: Token store to presist and verify request uniqueness

## Typical Flow (e.g., MQ, DB or HTTP Request)

- Operation trigger (message received, API called, job executed)
- Aspect intercepts and checks Redis for uniqueness key
- If key exists(in Redis Store):
    - Skip execution or raise `RepeatConsumptionException`
- If key missing:
    - Proceed to logic
    - Store token to Redis Store
- Post-processing clears or confirms token depending on outcome.

## Integration Test: Examples for Message Consumer

In `activemq-idem-app` module:

```java
import org.springframework.stereotype.Component;

@Component
public class SomeConsumerService {
    @Idempotent(scene = "ORDER", type = Idempotent.Type.MQ)
    public void process(String payload) {
        // impl logic here 
    }
}

// ---

@SpringBootTest
@ActiveProfiles("test")
public class ActiveMQIdempotentIT {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private SomeConsumerService service;

    @Test
    public void testIdempotentMessageHandling() throws Exception {
        String payload = "{\"orderId\":\"123\"}";
        jmsTemplate.convertAndSend("idem.mq.test.queue", payload);

        Thread.sleep(100);

        // Send again 
        jmsTemplate.convertAndSend("idem.mq.test.queue", payload);

        // Should only be processed once
        verify(service, times(1)).process(any());
    }
}
```

## Benchmark Plan (JMH or Custom Timer)

### JMH Sample

```java

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Thread)
public class IdempotentBenchmark {

    private IdempotentExecuteHandler handler;

    @Setup
    public void setup() {
        handler = new RedisIdempotentExecuteHandler();
    }

    @Benchmark
    public void testExecute() throws Throwable {
        ProceedingJoinPoint joinPoint = ...;
        handler.execute(joinPoint, mock(Idempotent.class));
    }
}
```

### StopWatch + actuator metrics in local env:

```java
import org.springframework.util.StopWatch;

StopWatch sw = new StopWatch(); 
sw.

start(); 
handler.

execute(joinPoint, idempotent); 
sw.

stop();
System.out.

println("Time taken: "+sw.getTotalTimeMillis()); 
```

## Deployment & CI/CD

### Jenkins pipeline:

- Build all `*-app` modules
- Run unit & integration tests
- Build Docker images
- Apply `terraform` to provision AWS resources (RDS, EKS, SQS)
- Deploy via `kubectl` or Helm

### Local Testing with Docker Compose:

```shell
cd deployment/docker-compose 
docker-compose up --build 
```

Includes Redis, ActiveMQ, HTTP Idempotent Apps.

### K8S + Monitoring

```shell
kubectl apply -f deployment/k8s/
open http://<grafana-ip>:3000
```

- Prometheus scrapes metrics from each `*-app`
- Grafana dashboards auto-imported from `monitoring/grafana`
- Redis, MQ brokers, and apps are automatically exposed via NodePort or Ingress 

