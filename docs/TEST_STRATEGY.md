# Test strategy: Swag Labs demo shop (Selenium)

**Version:** 1.0  |  **Prepared by:** QA (SDET)

## 1. Objective
Show a clean Selenium WebDriver framework (Java, TestNG, Page Object Model) that tests the main shopping flow of a public demo shop: login, products, cart and checkout.

## 2. Application under test

| Application | URL | Purpose |
| --- | --- | --- |
| Swag Labs (Sauce Demo) | https://www.saucedemo.com | Demo web shop. Demo users are public. |

## 3. Scope

| Module | Document | Cases | Type |
| --- | --- | --- | --- |
| Login | [login.md](test-cases/login.md) | 6 | Functional, Negative |
| Shopping | [shopping.md](test-cases/shopping.md) | 4 | Functional, End to end |

**Out of scope:** the special demo users that misbehave on purpose (`problem_user`, `performance_glitch_user`), visual testing, performance, accessibility, mobile browsers, cross-browser runs in CI (Chrome only; Firefox and Edge are supported through `-Dbrowser`).

## 4. Approach

| Topic | How |
| --- | --- |
| Design | Page Object Model: `LoginPage`, `InventoryPage`, `CartPage`, `CheckoutPage`; tests contain only assertions |
| Waits | Explicit waits only (`BasePage`), no `Thread.sleep` |
| Data-driven | TestNG data provider for the invalid login cases (first value is the test case ID) |
| Drivers | Selenium Manager downloads the driver automatically; headless by default |
| Reports | A TestNG listener writes a module-wise HTML report with a screenshot per case, a run CSV and a history index |

## 5. Environment and data
- Java 17, Maven 3.9, Selenium 4.27, TestNG 7.10, Chrome (headless).
- Public demo credentials only (`standard_user` / `secret_sauce`); no secrets in the repository.
- The cart lives in the browser; every test starts with a fresh browser, so nothing needs clean-up.

## 6. Entry and exit criteria
- Entry: https://www.saucedemo.com is reachable and Chrome is installed.
- Exit: every High priority case passes; the whole suite passes (it is small and deterministic).

## 7. Risks

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Public demo site is slow or its text changes | Failing tests that are not product bugs | Locators use the stable `data-test` attributes; texts are kept in one place per case |
| Browser or driver version mismatch | Tests cannot start | Selenium Manager picks a matching driver |

## 8. Deliverables
Test case documents and master CSVs (`docs/test-cases`), automation code, module-wise reports of every run (`reports/`), a traceability check (`node tools/docs.js check`) and a CI pipeline that also publishes the reports.
