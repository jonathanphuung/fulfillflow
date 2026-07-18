# FulfillFlow

FulfillFlow is an order fulfillment and inventory management API. It models the workflow of reserving stock, assigning fulfillment tasks, and tracking an order from submission to completion.

## Stack

- Java 21 and Spring Boot
- PostgreSQL and Flyway
- RabbitMQ
- Redis
- Maven
- Docker Compose

## Local setup

Install Java 21 and Docker, then start PostgreSQL:

```bash
docker compose up -d
```

Run the API:

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`.

The health endpoint is available at `http://localhost:8080/actuator/health`. RabbitMQ management is available at `http://localhost:15672` using `fulfillflow` for both the local username and password.

## Authentication

Set `ADMIN_EMAIL`, `ADMIN_PASSWORD`, and a random `JWT_SECRET` containing at least 32 characters before the first startup. The bootstrap administrator is created only when no account with that email exists.

Request a one-hour access token:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"your-password"}'
```

Send the returned token as `Authorization: Bearer <token>`. Administrators manage products, inventory, and user accounts. Workers manage orders and fulfillment tasks.
