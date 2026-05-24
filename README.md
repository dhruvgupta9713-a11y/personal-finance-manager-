# Personal Finance Manager API

A REST API for managing personal finances built with Spring Boot. You can track income/expenses, set savings goals, and generate reports.

I built this as a project to learn Spring Boot and REST APIs. It uses session-based authentication (no JWT) and stores data in an H2 file-based database.

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security (session-based auth)
- Spring Data JPA
- H2 Database (file-based)
- Jakarta Validation
- Lombok
- Maven

## How to Run

Make sure you have Java 17 and Maven installed, then:

```bash
# clone the repo and cd into it
mvn spring-boot:run
```

The app runs on `http://localhost:8080`

### H2 Console

You can access the H2 database console at:
```
http://localhost:8080/h2-console
```
- JDBC URL: `jdbc:h2:file:./financedb`
- Username: `sa`
- Password: (leave empty)

## API Endpoints

### Auth (`/api/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login (sets session cookie) |
| POST | `/api/auth/logout` | Logout |

### Categories (`/api/categories`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/categories` | Get all categories (default + custom) |
| POST | `/api/categories` | Create custom category |
| DELETE | `/api/categories/{name}` | Delete custom category |

### Transactions (`/api/transactions`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions` | Create transaction |
| GET | `/api/transactions` | Get all (supports `startDate`, `endDate`, `categoryId` filters) |
| PUT | `/api/transactions/{id}` | Update transaction (date cant be changed) |
| DELETE | `/api/transactions/{id}` | Delete transaction |

### Goals (`/api/goals`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/goals` | Create savings goal |
| GET | `/api/goals` | Get all goals with progress |
| GET | `/api/goals/{id}` | Get goal by id (includes progress) |
| PUT | `/api/goals/{id}` | Update goal |
| DELETE | `/api/goals/{id}` | Delete goal |

### Reports (`/api/reports`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reports/monthly/{year}/{month}` | Monthly report |
| GET | `/api/reports/yearly/{year}` | Yearly report |

## Default Categories

The app comes with some default categories pre-loaded on startup:

**Income:** Salary

**Expense:** Food, Rent, Transportation, Entertainment, Healthcare, Utilities

You can also create your own custom categories through the API.

## Authentication

This project uses **session-based authentication** instead of JWT. When you login, a JSESSIONID cookie is set. You need to include this cookie in all subsequent requests.

> **Note:** If you're using Postman, make sure cookies are enabled so the session cookie gets sent automatically.

## Running Tests

```bash
mvn test
```

Tests use JUnit 5 and Mockito. Each service class has its own test file with mocked repos.

## Example Requests

### Register
```json
POST /api/auth/register
{
    "username": "user@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "phoneNumber": "+1234567890"
}
```

### Create Transaction
```json
POST /api/transactions
{
    "amount": 50000.00,
    "date": "2024-01-15",
    "category": "Salary",
    "description": "January salary"
}
```

### Create Goal
```json
POST /api/goals
{
    "goalName": "Emergency Fund",
    "targetAmount": 5000.00,
    "targetDate": "2026-01-01",
    "startDate": "2025-01-01"
}
```

## Project Structure

```
src/
├── main/java/com/finance/manager/
│   ├── config/           # SecurityConfig, DataSeeder
│   ├── controller/       # REST controllers
│   ├── dto/
│   │   ├── request/      # Request DTOs with validation
│   │   └── response/     # Response DTOs
│   ├── entity/           # JPA entities
│   ├── exception/        # Custom exceptions + global handler
│   ├── repository/       # Spring Data repos
│   └── service/          # Business logic
└── test/java/com/finance/manager/
    └── service/          # Unit tests for all services
```

## TODO / Future Improvements

- [ ] Add pagination for transactions
- [ ] Switch to PostgreSQL for production
- [ ] Add budget feature (monthly budget per category)
- [ ] Add data export (CSV maybe)
- [ ] Maybe add JWT option later
- [ ] Add more detailed error messages
- [ ] Deploy somewhere

---

Made for learning purposes 🎓
