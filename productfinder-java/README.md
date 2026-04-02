# ProductFinder — Java / Spring Boot

A Walmart product search application rebuilt in Java from the original Python project.
Runs over **HTTP only** on `http://localhost:8080`.

---

## What the app does

1. Users create an account and log in.
2. They type a search term (e.g. "laptop", "headphones").
3. The server scrapes Walmart's search page and returns matching products with titles, prices, and availability.
4. Every search is saved to a local database; users can browse their history and re-load past results.
5. Users can delete individual searches from their history.

---

## Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Web | Spring Web (REST + Thymeleaf pages) |
| Security | Spring Security 6 + BCrypt + JWT (jjwt 0.12) |
| Database | H2 (file-based, persists between restarts) |
| Scraping | Jsoup 1.17 |
| JSON | Jackson |
| Logging | SLF4J + Logback |
| Build | Maven |

---

## How it maps from the original Python project

### Legacy backend scripts (current `master` branch)

| Python file | Java equivalent |
|---|---|
| `walmart.py` — BeautifulSoup scraper | `WalmartScraperService.java` (Jsoup) |
| `LocationZip.py` — SQLite zip lookup | H2 database via JPA (zip feature not ported — was standalone/incomplete) |
| `importSheets.py` / `sheet.py` — Google Sheets sync | Not ported; the Google Sheets pipeline is replaced by the local H2 database |
| `proxy.py` — proxy tester | Not ported; was a dev utility |

### FastAPI MVP (commit `e8707079`)

| Python / FastAPI | Java / Spring Boot |
|---|---|
| `FastAPI` app | `Spring Boot` + `@RestController` |
| `SQLAlchemy` models | JPA `@Entity` classes (User, Search, Product) |
| `Pydantic` schemas | Java DTOs with `@Valid` Bean Validation |
| `python-jose` JWT | `io.jsonwebtoken` (jjwt) |
| `passlib[bcrypt]` | `BCryptPasswordEncoder` (Spring Security) |
| `beautifulsoup4` | Jsoup |
| `uvicorn` ASGI server | Embedded Tomcat (Spring Boot) |
| `SQLite` database | H2 file-based database |
| `routes/auth.py` | `AuthController.java` + `UserService.java` |
| `routes/search.py` | `SearchController.java` + `SearchService.java` |
| `scraper.py` | `WalmartScraperService.java` |
| `database.py` | `User.java`, `Search.java`, `Product.java` + repositories |
| `config.py` | `application.properties` |
| `auth_utils.py` | `JwtUtil.java`, `JwtAuthFilter.java` |
| `frontend/*.html` | `src/main/resources/templates/*.html` (Thymeleaf) |
| `static/css/style.css` | `src/main/resources/static/css/style.css` |
| `static/js/*.js` | `src/main/resources/static/js/*.js` |

### What changed because of Java / HTTP

- **No HTTPS** — the original Python project also used HTTP locally. This Java version explicitly disables any HTTPS concerns.
- **No ASGI** — Uvicorn is replaced by embedded Tomcat (Spring Boot default).
- **No `.env` file** — configuration lives in `application.properties`. Change `app.jwt.secret` before any non-local use.
- **No Google Sheets** — the legacy Google Sheets pipeline was offline-only tooling. It is not ported.
- **H2 instead of SQLite** — H2 in file mode behaves identically to SQLite for this use case and requires zero extra setup on Windows or Linux.
- **BCrypt strength** — defaults to Spring Security's BCrypt strength 10 (same effective security as passlib bcrypt).
- **JWT field naming** — Spring Boot uses camelCase JSON by default, so the token field is `accessToken` instead of `access_token`. The JS frontend is updated accordingly.

---

## Prerequisites

- Java 21+
- Maven 3.8+ (or use the included `mvnw` wrapper if added)

Check with:
```bash
java -version
mvn -version
```

---

## How to run

```bash
cd productfinder-java
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

### Build a fat JAR and run it

```bash
mvn package -DskipTests
java -jar target/productfinder-1.0.0.jar
```

### Run tests

```bash
mvn test
```

---

## Example HTTP URLs

All URLs are plain HTTP — no TLS required.

| URL | Description |
|---|---|
| `http://localhost:8080/` | Landing page |
| `http://localhost:8080/register` | Create account |
| `http://localhost:8080/login` | Login |
| `http://localhost:8080/search` | Search page (requires login) |
| `http://localhost:8080/h2-console` | H2 database console (dev) |
| `http://localhost:8080/health` | Health check |

### API endpoints (curl examples)

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}'
# → {"accessToken":"eyJ...","tokenType":"bearer"}

# Search (replace TOKEN with the value from login)
curl -X POST http://localhost:8080/api/search/ \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"query":"laptop"}'

# Search history
curl http://localhost:8080/api/search/history \
  -H "Authorization: Bearer TOKEN"

# Load a specific past search
curl http://localhost:8080/api/search/1 \
  -H "Authorization: Bearer TOKEN"

# Delete a search
curl -X DELETE http://localhost:8080/api/search/1 \
  -H "Authorization: Bearer TOKEN"
```

---

## Project structure

```
productfinder-java/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/productfinder/
    │   │   ├── ProductFinderApplication.java   ← entry point
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java          ← Spring Security (JWT, no HTTPS)
    │   │   │   └── AppConfig.java               ← Jackson config
    │   │   ├── controller/
    │   │   │   ├── PageController.java          ← serves HTML pages
    │   │   │   ├── AuthController.java          ← /api/auth/*
    │   │   │   ├── SearchController.java        ← /api/search/*
    │   │   │   ├── HealthController.java        ← /health
    │   │   │   └── GlobalExceptionHandler.java  ← error responses
    │   │   ├── dto/                             ← request/response DTOs
    │   │   ├── model/                           ← JPA entities (User, Search, Product)
    │   │   ├── repository/                      ← Spring Data JPA repos
    │   │   ├── security/
    │   │   │   ├── JwtUtil.java                 ← token generation/validation
    │   │   │   ├── JwtAuthFilter.java           ← per-request JWT check
    │   │   │   └── UserDetailsServiceImpl.java  ← Spring Security user loading
    │   │   └── service/
    │   │       ├── UserService.java             ← register, login, me
    │   │       ├── SearchService.java           ← search CRUD + orchestration
    │   │       └── WalmartScraperService.java   ← Jsoup scraper
    │   └── resources/
    │       ├── application.properties
    │       ├── templates/                       ← Thymeleaf HTML pages
    │       └── static/
    │           ├── css/style.css
    │           └── js/                          ← vanilla JS (login, register, search)
    └── test/
        ├── java/com/productfinder/
        │   ├── ProductFinderApplicationTests.java
        │   ├── controller/AuthControllerTest.java
        │   └── service/WalmartScraperServiceTest.java
        └── resources/
            └── application-test.properties      ← in-memory H2 for tests
```

---

## Configuration reference (`application.properties`)

| Property | Default | Notes |
|---|---|---|
| `server.port` | `8080` | HTTP port |
| `app.jwt.secret` | *(see file)* | **Change this** before any deployment |
| `app.jwt.expiration-ms` | `1800000` | 30 minutes |
| `app.scraper.max-results` | `20` | Max products returned per search |
| `app.scraper.timeout-ms` | `12000` | HTTP timeout for Walmart requests |
| `app.scraper.user-agent` | Chrome UA | Sent with scrape requests |
| `spring.h2.console.enabled` | `true` | Disable in non-dev environments |

---

## Known limitations

1. **Walmart anti-bot** — Walmart actively blocks automated requests. Searches may return zero results when their detection is triggered. This is a limitation of the original Python scraper too; the Java version behaves identically.
2. **No rate limiting** — There is no per-user rate limit on search requests. Add Spring's `@RateLimiter` or a filter if deploying beyond localhost.
3. **JWT stored in localStorage** — The original frontend design. Fine for local use; prefer HttpOnly cookies for public deployment.
4. **Single-node H2** — H2 file mode does not support multiple JVM instances against the same file. Fine for local/single-server use.
5. **No Google Sheets sync** — The legacy `importSheets.py` / `sheet.py` pipeline is not ported. It was local tooling dependent on a private Google service account.

---

## Future improvements

- Replace H2 with PostgreSQL for multi-user production deployments.
- Add per-user search rate limiting.
- Cache recent search results to reduce Walmart requests.
- Add a proxy/rotation layer to improve scraper reliability.
- Move JWT from localStorage to HttpOnly cookies.
- Add pagination to search history.
- Add product image extraction (if available in scrape response).
