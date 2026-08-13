# NeoBank - Account Service (Core Banking)

**Java 21 + Spring Boot 3.3** ready-to-run Account Service.

This is the **Core** Account Service of the NeoBank project.

---

## Features

- Create Account (Savings / Current / FD / RD)
- Get Account by Account Number or ID
- Get all accounts of a Customer
- Update Account Status (ACTIVE → FROZEN → CLOSED etc.)
- Close Account
- Account Status History (audit)
- Optimistic Locking
- Account Number generation
- Swagger UI
- H2 in-memory DB (ready for PostgreSQL)

---

## Tech Stack

- Java 21
- Spring Boot 3.3.2
- Spring Data JPA
- PostgreSQL / H2
- Lombok
- Springdoc OpenAPI (Swagger)
- Validation
- Actuator

---

## How to Run

### Option 1: Quick Start (H2 - No DB installation needed)

```bash
cd account-service
./mvnw spring-boot:run
```

Or if you have Maven installed:

```bash
mvn spring-boot:run
```

### Option 2: Using PostgreSQL

1. Create database:
```sql
CREATE DATABASE account_db;
```

2. Change profile in `application.yml`:
```yaml
spring:
  profiles:
    active: postgres
```

3. Run the application.

---

## Access Points

| Resource              | URL                                      |
|-----------------------|------------------------------------------|
| Swagger UI            | http://localhost:8081/swagger-ui.html    |
| API Docs              | http://localhost:8081/api-docs           |
| H2 Console            | http://localhost:8081/h2-console         |
| Health                | http://localhost:8081/actuator/health    |

**H2 Credentials**:  
JDBC URL: `jdbc:h2:mem:accountdb`  
User: `sa`  
Password: (empty)

---

## Sample API Calls

### 1. Create Account
```bash
curl -X POST http://localhost:8081/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1001,
    "productId": 1,
    "accountType": "SAVINGS",
    "branchCode": "001",
    "currency": "INR"
  }'
```

### 2. Get Account
```bash
curl http://localhost:8081/api/v1/accounts/{accountNumber}
```

### 3. Get Accounts by Customer
```bash
curl http://localhost:8081/api/v1/accounts/customer/1001
```

### 4. Freeze Account
```bash
curl -X PATCH http://localhost:8081/api/v1/accounts/{accountNumber}/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "FROZEN",
    "reason": "Suspicious activity",
    "changedBy": "ADMIN"
  }'
```

### 5. Close Account
```bash
curl -X POST http://localhost:8081/api/v1/accounts/{accountNumber}/close \
  -H "Content-Type: application/json" \
  -d '{"reason": "Customer request"}'
```

---

## Project Structure

```
account-service/
├── src/main/java/com/neobank/account/
│   ├── controller/          # REST Controllers
│   ├── service/             # Business Logic
│   ├── repository/          # JPA Repositories
│   ├── entity/              # JPA Entities
│   ├── dto/                 # Request/Response DTOs
│   ├── enums/               # AccountStatus, AccountType
│   ├── exception/           # Custom Exceptions + Handler
│   └── AccountServiceApplication.java
├── src/main/resources/
│   └── application.yml
├── pom.xml
└── README.md
```

---

## Next Steps (for full NeoBank)

1. Ledger Service (Double-entry)
2. Transfer / Payments Service
3. Customer Service
4. API Gateway
5. Kafka for events
6. Docker Compose

---

**Happy Coding!**  
This service is ready for interviews and further extension.
