# Selenium

**Automation Framework:** Selenium WebDriver 4 + Java 17 + TestNG using the Page Object Model, running against the public demo shop [saucedemo.com](https://www.saucedemo.com).

## What is covered
| Area | Tests |
|------|-------|
| Login | valid login, locked-out user, 4 invalid-credential cases (TestNG `@DataProvider`) |
| Inventory | cart badge updates, sort by price (low to high) |
| Cart / Checkout | cart contents, end-to-end checkout to "Thank you for your order!" |

## Structure
```
Selenium/
├── pom.xml                      # Selenium 4.27, TestNG 7.10, Surefire
├── testng.xml                   # suite definition
├── src/main/java/com/allahditta/
│   ├── pages/                   # BasePage, LoginPage, InventoryPage, CartPage, CheckoutPage (POM)
│   └── utils/DriverFactory.java # chrome / firefox / edge, headless switch
├── src/test/java/com/allahditta/tests/   # BaseTest, LoginTest, ShoppingTest
└── .github/workflows/ci.yml     # GitHub Actions (headless Chrome)
```

## Prerequisites
- JDK 17+ and Maven 3.9+
- Chrome (or Firefox/Edge). Driver binaries are downloaded automatically by Selenium Manager.

## Run
```bash
mvn test                      # headless Chrome (default)
mvn test -Dheadless=false     # watch the browser
mvn test -Dbrowser=firefox    # firefox | edge | chrome
```
Reports: `target/surefire-reports/`.

## Notes
Only the public demo credentials of saucedemo.com are used (`standard_user` / `secret_sauce`); no secrets are stored in the repo.

## Status
The project compiles (`mvn test-compile`). The first end-to-end run on the authoring machine was blocked by a local Java networking problem (`Unable to establish loopback connection` while creating the JDK HttpClient), so the browser tests are expected to run in CI (see the Actions tab) or on a normal machine - check the latest workflow run for the real result.
