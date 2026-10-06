# E-Commerce UI Tests: Java, Selenium, TestNG

A Selenium WebDriver test framework in Java for a React, Express and PostgreSQL online shop ([the same application used for the Playwright suite](https://github.com/mirzamaazbaig/Ecom)). The point of this repository is the framework: how it is structured, how it stays stable under parallel execution, and how it runs in CI.

- **29 UI tests**, run headless in about a minute with 3 classes in parallel; an 8-test smoke subset runs in under 20 seconds.
- **Page Object Model** with explicit waits only: no `Thread.sleep`, no implicit wait.
- **API-assisted setup:** users are created and signed in through the REST API, so only the tests that are about login use the login screen.
- **UI checked against the API:** the product list, category filter and prices shown in the browser are compared with what the API returns.
- **CI:** GitHub Actions starts the application with a PostgreSQL service container and runs the suite; reports and failure screenshots are uploaded as artifacts.

## What is tested

| Class | Tests | Coverage |
|---|---|---|
| `AuthTest` | 12 | Register, mismatched passwords, duplicate email, login, invalid credentials (data provider), logout, session after refresh, protected pages redirect (data provider) |
| `ProductBrowsingTest` | 7 | Catalogue matches the API, category filter, all departments, price sort, search, empty search, details page matches the card |
| `CartCheckoutTest` | 6 | Add from list, quantity from details page, remove, cart total, checkout creates an order and empties the cart, empty order history |
| `WishlistTest` | 4 | Empty by default, add from details, remove, move to cart |

Tests tagged `smoke` (8 of them) form the quick regression subset.

## Design decisions

- **Explicit waits, wrapped once.** `BasePage` owns a `WebDriverWait` and exposes `click`, `type`, `waitUntil`. `click` scrolls the element to the centre and retries when a layout shift intercepts it (images loading after the scroll), instead of failing on the first attempt.
- **Stale elements are expected, not exceptional.** The app is React: lists re-render while a test reads them. Waits ignore `StaleElementReferenceException`, and list reads on the product grid are done in a single script call so they are atomic. This was found by deliberately breaking the application and seeing one unrelated test fail intermittently.
- **One browser per test, one driver per thread.** `DriverFactory` keeps the driver in a `ThreadLocal`, so test classes can run in parallel (`testng.xml`, `parallel="classes"`).
- **Isolated test data.** Every test gets its own user (random email), so there is no ordering between tests and no shared state to clean up.
- **API for setup, UI for the behaviour under test.** `BaseTest.signedInUser()` registers through the API and hands the session cookie to the browser. `ApiClient` is also the source of truth for expected catalogue data; it is never used to assert the behaviour being tested.
- **Native alerts are handled explicitly.** The application confirms several actions with `alert()`; page methods that trigger one accept it and return its text for assertion.
- **Failure evidence.** `ScreenshotListener` writes `target/screenshots/<Class>.<method>.png` before the browser closes.
- **Assertions with AssertJ**, with failure messages that say which scenario failed.

## Project structure

```
src/main/java/qa/ecom/
  config/    Config            env vars, -D properties, config.properties defaults
  driver/    DriverFactory     ThreadLocal<WebDriver>, Chrome options
  api/       ApiClient         user creation, session cookie, catalogue data
  data/      TestUser          unique users
  pages/     BasePage, NavBar, LoginPage, RegisterPage, HomePage,
             ProductDetailsPage, CartPage, OrdersPage, WishlistPage
src/test/java/qa/ecom/
  base/      BaseTest, ScreenshotListener
  tests/     AuthTest, ProductBrowsingTest, CartCheckoutTest, WishlistTest
src/test/resources/  testng.xml (full), testng-smoke.xml
.github/workflows/   selenium.yml
```

## Running the tests

Prerequisites: JDK 21, Maven 3.9+, Chrome, and the application running.

1. Start the application (see its README): PostgreSQL, the API on `:5000` and the client on `:5173`.
2. Run the suite:

```bash
mvn test                                  # full suite, headless
mvn test -Dsuite=testng-smoke.xml         # smoke subset
mvn test -Dheadless=false                 # watch the browser
mvn test -DbaseUrl=http://localhost:5173 -DapiUrl=http://localhost:5000/api
```

Selenium Manager downloads a matching chromedriver automatically. Test reports are in `target/surefire-reports`, failure screenshots in `target/screenshots`.

### Configuration

Each setting can be given as an environment variable or a `-D` property; the defaults are in `src/main/resources/config.properties`.

| Setting | Env var | Property | Default |
|---|---|---|---|
| Application URL | `BASE_URL` | `baseUrl` | `http://localhost:5173` |
| API URL | `API_URL` | `apiUrl` | `http://localhost:5000/api` |
| Headless | `HEADLESS` | `headless` | `true` |
| Wait timeout (s) | `TIMEOUT_SECONDS` | `timeoutSeconds` | `10` |
| Chrome binary | `CHROME_BINARY` | `chromeBinary` | auto |
| chromedriver | `CHROMEDRIVER_PATH` | `chromedriverPath` | auto (Selenium Manager) |
| `--no-sandbox` | `NO_SANDBOX` | `noSandbox` | `false` |

The tests use seeded catalogue data and place orders, which use up stock. If a checkout test reports insufficient stock after many local runs, re-seed the application database.

## Continuous integration

[`.github/workflows/selenium.yml`](.github/workflows/selenium.yml) runs on every push and pull request, and can be started manually with a chosen application ref and suite:

1. Checks out this repository and the application repository.
2. Starts PostgreSQL as a service container, creates and seeds the database.
3. Starts the API and the client and waits until both answer.
4. Runs `mvn test` on JDK 21 with Chrome from the runner.
5. Uploads `surefire-reports`, and on failure the screenshots and application logs.

## Relation to the Playwright suite

The Playwright repository covers the same application with UI and API tests and finds defects at the API level. This repository is deliberately narrower: UI behaviour only, in Java, to show the Selenium and TestNG toolset and framework design. The flows overlap on purpose so that the two tools can be compared.

## Limitations and next steps

- Chrome only. A `BROWSER` option and Firefox support would be the next addition to `DriverFactory`.
- No reporting beyond TestNG and Surefire output; an Allure or Extent report would give a browsable HTML report.
- The admin dashboard and the review form are not covered.
