# Bedtime stories website

### Overview
A full-featured Java Spring Boot backend for a bedtime stories platform. Includes user authentication, OAuth2 login, email support, PostgreSQL database, Redis caching, and Dockerized deployment.

➡️ Frontend: [fairy-tales-frontend](https://github.com/StudentPP1/fairy-tales-backend)

---

### Tech Stack
- **Java 17**
- **Spring Boot 3.4.5**
- **Spring Security + OAuth2 Authorization Server**
- **JWT (JSON Web Tokens)**
- **Spring Data JPA (PostgreSQL)**
- **Liquibase** – database migrations
- **Redis** – caching and sessions
- **Spring Mail** – email sending
- **Thymeleaf** – templating engine
- **Dotenv** (`spring-dotenv`)
- **Lombok**
- **Testcontainers** – testing with PostgreSQL containers
- **Docker & Docker Compose**

---

### Configuration
Set up environment variables in a `.env` file:
```dotenv
DB_USERNAME=
DB_PASSWORD=
DB_NAME=
FRONT_END_URL=
JWT_SECRET_KEY=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
MAIL_PASSWORD=
MAIL_USER_EMAIL=
```

---

### Run tests
This project includes integration and repository tests using JUnit 5, Spring Boot Test, and Testcontainers:

- `StoryRepositoryTest` – tests story-related queries
- `UserRepositoryTest` – tests user-related queries

---

### How to Run Application
Clone the repository:
```
git clone https://github.com/StudentPP1/fairy-tales-backend.git
cd fairy-tales-backend
```

Run docker compose:
```
docker-compose up
```
