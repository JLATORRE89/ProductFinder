# ProductFinder — Task Tracker

---

## Completed

### [DONE] Initial Python backend scripts
- `backend/walmart.py` — CLI Walmart scraper (BeautifulSoup, CSV output)
- `backend/importSheets.py` — Google Sheets CSV import pipeline
- `backend/sheet.py` — Google Sheets data sync
- `backend/LocationZip.py` — Zip code geolocation lookup via OpenDataSoft API → SQLite
- `backend/proxy.py` — Proxy connection test utility

### [DONE] Java / Spring Boot rebuild (`productfinder-java/`)
Full translation of the Python project into a Java 21 + Spring Boot 3.2 application.

**Infrastructure**
- [x] `pom.xml` — Maven build with Spring Boot, Spring Security, JPA, H2, Jsoup, jjwt, Lombok
- [x] `application.properties` — HTTP-only (port 8080), H2 file DB, JWT config, scraper config
- [x] `application-test.properties` — in-memory H2 for tests

**Models / Persistence**
- [x] `User.java` — JPA entity (id, email, username, hashedPassword, createdAt)
- [x] `Search.java` — JPA entity (id, user FK, query, createdAt, products list)
- [x] `Product.java` — JPA entity (id, search FK, title, url, price, status, createdAt)
- [x] `UserRepository`, `SearchRepository`, `ProductRepository` — Spring Data JPA

**Security**
- [x] `JwtUtil.java` — HS256 token generation and validation (jjwt 0.12)
- [x] `JwtAuthFilter.java` — per-request Bearer token filter
- [x] `UserDetailsServiceImpl.java` — Spring Security user loading
- [x] `SecurityConfig.java` — stateless filter chain, CSRF disabled, public/protected route split

**Services**
- [x] `UserService.java` — register, login (BCrypt), me
- [x] `SearchService.java` — execute search, history, get by ID, delete
- [x] `WalmartScraperService.java` — Jsoup scraper with primary + fallback parsing

**Controllers**
- [x] `PageController.java` — serves the four Thymeleaf pages
- [x] `AuthController.java` — `/api/auth/register`, `/api/auth/login`, `/api/auth/me`
- [x] `SearchController.java` — `/api/search/`, `/api/search/history`, `/api/search/{id}` (GET/DELETE)
- [x] `HealthController.java` — `/health`
- [x] `GlobalExceptionHandler.java` — unified JSON error responses

**DTOs**
- [x] `RegisterRequest`, `LoginRequest`, `SearchRequest` (validated)
- [x] `UserResponse`, `TokenResponse`, `ProductResponse`, `SearchResponse`, `SearchHistoryResponse`, `ErrorResponse`

**Frontend (Thymeleaf + vanilla JS)**
- [x] `templates/index.html` — landing page
- [x] `templates/login.html` — login form
- [x] `templates/register.html` — registration form
- [x] `templates/search.html` — search + history page
- [x] `static/css/style.css` — full responsive stylesheet
- [x] `static/js/login.js` — login form handler
- [x] `static/js/register.js` — registration form handler
- [x] `static/js/search.js` — search, history, delete, XSS escaping

**Tests**
- [x] `ProductFinderApplicationTests.java` — context load smoke test
- [x] `AuthControllerTest.java` — register, login, duplicate user, bad password
- [x] `WalmartScraperServiceTest.java` — ProductData record, graceful network failure

**Documentation**
- [x] `README.md` — full migration guide, run instructions, API examples, project structure
- [x] `.gitignore` — Maven target, H2 db files, Python venv, secrets, IDE folders, OS noise

---

## Upcoming

### [TODO] Environment setup
- [ ] Install Java 21 on dev machine (not currently installed)
- [ ] Install Maven 3.8+ (or use `mvnw` wrapper — see note below)
- [ ] Verify first build: `cd productfinder-java && mvn spring-boot:run`
- [ ] Add Maven Wrapper (`mvn wrapper:wrapper`) so no global Maven install is needed

### [TODO] First-run verification
- [ ] Register a test user via `http://localhost:8080/register`
- [ ] Log in and confirm JWT is issued
- [ ] Run a product search and confirm results appear (or graceful empty state)
- [ ] Verify search history sidebar updates correctly
- [ ] Delete a history entry and confirm it disappears
- [ ] Open `http://localhost:8080/h2-console` and confirm tables exist with data

### [TODO] Scraper hardening
- [ ] Test against live Walmart — confirm primary CSS selector (`data-item-id`, `span.w_iUH7`) still matches current DOM
- [ ] Update CSS selectors in `WalmartScraperService.java` if Walmart has changed their markup
- [ ] Consider adding a random delay or jitter between requests to reduce bot detection
- [ ] Evaluate adding a proxy/rotation option (config flag in `application.properties`)
- [ ] Add a mock HTML scraper test that parses a saved Walmart HTML fixture

### [TODO] JWT secret rotation
- [ ] Replace the placeholder secret in `application.properties` with a strong random value
- [ ] Document the secret rotation process in README

### [TODO] Quality / polish
- [ ] Add Maven Wrapper files (`mvnw`, `.mvn/wrapper/`) for one-command runs without Maven installed
- [ ] Add input sanitization for the search query (strip leading/trailing whitespace — partially done, review edge cases)
- [ ] Add pagination to search history (currently capped at 20 via `limit` param)
- [ ] Add loading spinner to history panel on page load
- [ ] Show a user-friendly message when Walmart scrape returns 0 results vs. when a network error occurs

### [TODO] Testing
- [ ] Add `SearchControllerTest.java` — authenticated search POST, history GET, delete
- [ ] Add scraper HTML fixture test — save a real Walmart response HTML and assert parsing output
- [ ] Add `UserServiceTest.java` — unit tests for register validation and login logic
- [ ] Run full test suite: `mvn test`

### [TODO] Optional future features
- [ ] Port `LocationZip.py` geolocation — store zip → city/state lookups in H2
- [ ] Add product image extraction to scraper if Walmart markup includes image URLs
- [ ] Add export to CSV (reproduce the original `walmart.csv` output, downloadable via browser)
- [ ] Add product detail page/modal
- [ ] Add search result filtering (by price range, availability)
- [ ] Replace H2 with PostgreSQL for multi-user or non-local deployment
