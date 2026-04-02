# ProductFinder — Agent Handoff Guide

This document gives any incoming agent (human or AI) everything needed to pick up work on this project without prior context.

---

## What this project is

A Walmart product search web application. Users register, log in, search for products by keyword, and browse their search history. The server scrapes Walmart's search results page and returns product titles, URLs, prices, and availability.

---

## Repository layout

```
c:\projects\ProductFinder\
├── .gitignore
├── tasks.md                  ← task tracker (read this first)
├── agents.md                 ← this file
├── backend/                  ← original Python scripts (legacy, not the active app)
│   ├── walmart.py            ← CLI Walmart scraper (BeautifulSoup)
│   ├── importSheets.py       ← Google Sheets CSV import
│   ├── sheet.py              ← Google Sheets data sync
│   ├── LocationZip.py        ← zip code → city lookup, SQLite output
│   └── proxy.py              ← proxy test utility
└── productfinder-java/       ← ACTIVE application (Java 21 + Spring Boot 3.2)
    ├── pom.xml
    ├── README.md             ← full run instructions and API docs
    └── src/
        ├── main/java/com/productfinder/
        │   ├── config/       ← SecurityConfig (JWT, HTTP-only), AppConfig (Jackson)
        │   ├── controller/   ← PageController, AuthController, SearchController, HealthController, GlobalExceptionHandler
        │   ├── dto/          ← request/response DTOs (validated with @Valid)
        │   ├── model/        ← JPA entities: User, Search, Product
        │   ├── repository/   ← Spring Data JPA repos
        │   ├── security/     ← JwtUtil, JwtAuthFilter, UserDetailsServiceImpl
        │   └── service/      ← UserService, SearchService, WalmartScraperService
        ├── main/resources/
        │   ├── application.properties
        │   ├── templates/    ← Thymeleaf: index, login, register, search
        │   └── static/       ← css/style.css, js/login.js, js/register.js, js/search.js
        └── test/             ← ProductFinderApplicationTests, AuthControllerTest, WalmartScraperServiceTest
```

---

## How to run

**Prerequisites:** Java 21, Maven 3.8+

```bash
cd c:\projects\ProductFinder\productfinder-java
mvn spring-boot:run
# App starts at http://localhost:8080 — HTTP only, no HTTPS
```

**Run tests:**
```bash
mvn test
```

**Build fat JAR:**
```bash
mvn package -DskipTests
java -jar target/productfinder-1.0.0.jar
```

---

## Key files to read before touching anything

| File | Why it matters |
|---|---|
| [productfinder-java/src/main/resources/application.properties](productfinder-java/src/main/resources/application.properties) | All config: port, JWT secret, DB URL, scraper settings |
| [productfinder-java/src/main/java/com/productfinder/config/SecurityConfig.java](productfinder-java/src/main/java/com/productfinder/config/SecurityConfig.java) | Controls what's public vs. protected; JWT filter wired here |
| [productfinder-java/src/main/java/com/productfinder/service/WalmartScraperService.java](productfinder-java/src/main/java/com/productfinder/service/WalmartScraperService.java) | Jsoup scraper — CSS selectors break when Walmart updates their DOM |
| [productfinder-java/src/main/java/com/productfinder/security/JwtUtil.java](productfinder-java/src/main/java/com/productfinder/security/JwtUtil.java) | Token generation and validation; secret key loaded from properties |
| [productfinder-java/src/main/java/com/productfinder/service/SearchService.java](productfinder-java/src/main/java/com/productfinder/service/SearchService.java) | Orchestrates scrape → persist → return; main business logic path |
| [productfinder-java/src/main/resources/static/js/search.js](productfinder-java/src/main/resources/static/js/search.js) | All frontend behavior: search submit, history, delete, auth redirect |

---

## Architecture decisions (don't change without understanding why)

**HTTP only, no HTTPS**
Intentional. CSRF is disabled; sessions are stateless JWT. Do not add TLS redirects or `require-ssl` config for local dev.

**H2 file-based database**
Data persists in `productfinder-data.mv.db` in the working directory. For tests, an in-memory H2 is used via `application-test.properties`. Do not switch to SQLite without removing H2 dependencies — H2 covers the same use case with zero extra setup.

**JWT stored in localStorage**
Matches the original Python frontend design. Fine for local use. If moving to a public deployment, switch to HttpOnly cookies and re-enable CSRF.

**`@AuthenticationPrincipal User user`**
The `JwtAuthFilter` sets the authenticated principal to the full `User` JPA entity (not a Spring Security `UserDetails` wrapper). Controllers receive the `User` object directly. Do not add a `UserDetails` wrapper without updating every controller that uses `@AuthenticationPrincipal`.

**Scraper returns empty list on failure**
`WalmartScraperService.scrapeProducts()` never throws. It logs the error and returns `List.of()`. The search is still saved to the DB with zero products. This is intentional — don't let a scrape failure destroy the user's search record or return a 500.

**`SearchResponse` eagerly loads products**
`Search.getProducts()` is called inside `@Transactional` methods. If you add lazy loading or move logic outside a transaction, you will get `LazyInitializationException`. Keep product access inside service-layer transactions.

---

## API surface

All API endpoints are under `/api/`. Pages are served at `/`, `/login`, `/register`, `/search`.

```
POST   /api/auth/register      — { username, email, password } → UserResponse (201)
POST   /api/auth/login         — { username, password } → { accessToken, tokenType } (200)
GET    /api/auth/me            — Bearer token → UserResponse (200)

POST   /api/search/            — Bearer + { query } → SearchResponse with products (201)
GET    /api/search/history     — Bearer + ?limit=20 → List<SearchHistoryResponse> (200)
GET    /api/search/{id}        — Bearer → SearchResponse (200) or 404
DELETE /api/search/{id}        — Bearer → 204 or 404

GET    /health                 — { status: "healthy" } (200, no auth)
GET    /h2-console/**          — H2 web console (dev only, no auth)
```

Error responses always use the shape `{ "detail": "message string" }`.

---

## Common failure modes and fixes

**Scraper returns 0 products**
Walmart updated their DOM. Check `WalmartScraperService.java` — update the CSS selectors in `parsePrimary()`. Use browser DevTools on a Walmart search page to find the new class names for product title, price, and container.

**`LazyInitializationException` on products**
Some code is accessing `search.getProducts()` outside a `@Transactional` boundary. Ensure the call is inside a service method annotated `@Transactional`.

**JWT 401 on every request**
Check `app.jwt.secret` in `application.properties`. If it is under 32 characters, jjwt will reject it at startup. Minimum 32 ASCII characters for HS256.

**H2 console shows no tables**
`spring.jpa.hibernate.ddl-auto=update` creates tables on first run. If the DB file is corrupted, delete `productfinder-data.mv.db` and restart — it will be recreated empty.

**`BeanCreationException` on startup**
Usually a circular dependency or missing `@Component`. Run `mvn spring-boot:run` and read the full stack trace — Spring Boot prints the dependency chain clearly.

---

## What the legacy Python scripts do (for reference only)

The `backend/` directory is **not used by the running app**. It is the original work that was translated.

| Script | What it did |
|---|---|
| `walmart.py` | CLI: prompted for a search term, scraped Walmart, wrote `walmart.csv` |
| `importSheets.py` | Detected `walmart.csv`, uploaded it to a private Google Sheet via service account |
| `sheet.py` | Synced temp Google Sheet data to a main sheet |
| `LocationZip.py` | Read zip codes from a Google Sheet form, called OpenDataSoft API, stored results in `location.db` |
| `proxy.py` | One-off test that routed a request through a hardcoded proxy IP |

None of these are wired to the Java app. They can be deleted when the project is fully migrated.

---

## Next priorities (from tasks.md)

1. Install Java 21 and Maven, do a first `mvn spring-boot:run` to verify the build
2. Add Maven Wrapper (`mvn wrapper:wrapper`) so no global Maven install is needed
3. Verify live Walmart scraping — update CSS selectors in `WalmartScraperService.java` if needed
4. Add `SearchControllerTest.java` (authenticated search, history, delete)
5. Replace the placeholder JWT secret in `application.properties`

See [tasks.md](tasks.md) for the full list.
