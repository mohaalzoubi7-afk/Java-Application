# OrderFlow — two Spring Boot services (DevOps practice)

A self-contained learning application built with **Java 17, Spring Boot 3.3.5, Maven, Spring Data JPA, H2**, REST APIs, health endpoints, and Prometheus metrics.

**Intentionally excluded:** Dockerfiles, `.dockerignore`, Docker Compose, CI/CD workflows, Kubernetes manifests, monitoring configuration, reverse proxies and deployment scripts. You build those yourself.

## Services

| Service | Default port | Purpose |
| --- | --- | --- |
| inventory-service | 8081 | List/create products, restock, reserve, release inventory |
| order-service | 8082 | Place, list, view and cancel orders; calls inventory-service over HTTP |

Each service runs independently and stores data in its own **file-backed H2 database** under its working directory's `data/` folder. No external database needed to start. H2 is for learning; production would use a real DB.

## Requirements

- JDK 17+
- Maven 3.9+ (`mvn` on your PATH)

## Run locally (two terminals)

Terminal 1:
```bash
cd inventory-service
mvn spring-boot:run
```

Terminal 2 (from repository root in a new terminal):
```bash
cd order-service
mvn spring-boot:run
```

You can also build a runnable JAR for each service:
```bash
cd inventory-service && mvn clean package
java -jar target/inventory-service-1.0.0.jar
```

Run from the service folder so file DB paths are consistent.

## Manual test (curl)

1. List seeded products:
```bash
curl http://localhost:8081/api/products
```
2. Place an order (reduces `MOUSE-01` quantity from 30 to 28):
```bash
curl -i -X POST http://localhost:8082/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerEmail":"student@example.com","sku":"MOUSE-01","quantity":2}'
```
3. Verify inventory change:
```bash
curl http://localhost:8081/api/products/MOUSE-01
```
4. List orders:
```bash
curl http://localhost:8082/api/orders
```
5. Cancel the order (replace `1` with returned id) and verify stock restored:
```bash
curl -i -X POST http://localhost:8082/api/orders/1/cancel
curl http://localhost:8081/api/products/MOUSE-01
```
6. Trigger insufficient stock (`409 Conflict`):
```bash
curl -i -X POST http://localhost:8082/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerEmail":"student@example.com","sku":"MOUSE-01","quantity":99999}'
```
7. Add product (inventory API):
```bash
curl -i -X POST http://localhost:8081/api/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"MONITOR-01","name":"27-inch Monitor","price":220.00,"quantity":8}'
```
8. Restock product:
```bash
curl -i -X POST http://localhost:8081/api/products/MOUSE-01/restock \
  -H 'Content-Type: application/json' -d '{"quantity":5}'
```

## Ops endpoints

- Inventory health: `http://localhost:8081/actuator/health`
- Order health: `http://localhost:8082/actuator/health`
- Inventory Prometheus metrics: `http://localhost:8081/actuator/prometheus`
- Order Prometheus metrics: `http://localhost:8082/actuator/prometheus`

## Environment variables

| Variable | Service | Default | Meaning |
| --- | --- | --- | --- |
| `PORT` | both | 8081 / 8082 | HTTP server port |
| `INVENTORY_URL` | order | `http://localhost:8081` | How order service reaches inventory |
| `DB_URL` | both | `jdbc:h2:file:./data/...` | Database JDBC URL (unique per service) |
| `DB_USER` | both | `sa` | DB login |
| `DB_PASSWORD` | both | empty | DB password |

For Docker or Kubernetes, set `INVENTORY_URL` to your network-resolvable inventory service address (not `localhost`). **DB_URL for each service must remain distinct.** If persisting H2 in containers, explicitly think through working directory, bind mount/volume and database locks.

## Suggested DevOps tasks (no solutions included)

1. Package each service as a JAR and verify it runs outside an IDE.
2. Write two production-minded Dockerfiles, using multi-stage builds where appropriate.
3. Build, tag, scan, run and inspect images; arrange persistent storage for both DB files.
4. Create a network; start both containers and correctly set `INVENTORY_URL`.
5. Write your own `compose.yaml` with persistence, healthchecks and correct startup/readiness handling.
6. Set up GitHub Actions to compile, test, build, scan and publish both images.
7. Deploy on a Linux VM or EC2 instance; test service-to-service reachability.
8. Collect `/actuator/prometheus` with Prometheus and chart it in Grafana.
9. Test failures: stop inventory, cause a 409 response, restart order, try cancellation twice, and confirm persistence.
10. Optional hard level: orchestrate with Kubernetes and replace H2 with separate PostgreSQL databases.

## Known engineering limitations (intentional practice material)

This demo is **not production-safe for payment processing**. An HTTP stock reservation and a separate order database write are not one atomic transaction. The create flow includes a best-effort compensating release; it does not solve ambiguous network failures or duplicate retries. Cancellation is also vulnerable to concurrent requests. Authentication, idempotency keys, distributed tracing, retry policies/circuit breakers and a proper saga/outbox pattern are deliberately omitted. Diagnose and improve these if you want advanced backend + DevOps practice.
