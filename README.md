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

## Test cases
Every test has a documented case with the same ID, in the standard described in [docs/TEST_CASE_STANDARD.md](docs/TEST_CASE_STANDARD.md) (strategy: [docs/TEST_STRATEGY.md](docs/TEST_STRATEGY.md)).

| Module | Document | Master CSV | Cases |
|--------|----------|-----------|-------|
| Login | [login.md](docs/test-cases/login.md) | [login.csv](docs/test-cases/login.csv) | 6 |
| Shopping | [shopping.md](docs/test-cases/shopping.md) | [shopping.csv](docs/test-cases/shopping.csv) | 4 |
| **Total** | | | **10** (all automated) |

The ID is in the test: `@Test(description = "TC_LOGIN_01 - ...")` (for the data-driven login test the first data value is the ID).
The Markdown documents are the source of truth. After editing them:
```bash
node tools/docs.js csv      # regenerate the master CSVs (Node 18+, no packages)
node tools/docs.js check    # fails when documents, CSVs and code IDs drift apart (also runs in CI)
```

## Reports
The `ReportListener` (registered in `testng.xml`) writes module-wise reports of the real run - never edited by hand, old runs are kept:
```
reports/
├── index.html            # history, newest run first
├── history.json
└── login/  shopping/
    ├── <Module> test <DD-MM-YYYY>_<HH-mm-ss>.html         # self-contained, one card per test case with a screenshot
    └── <Module> test cases <DD-MM-YYYY>_<HH-mm-ss>.csv     # steps, expected result and the REAL result
```
Open [reports/index.html](reports/index.html) after cloning (GitHub shows HTML as text). Generate your own with `mvn test`.

**Where the committed reports come from (honest note):** the browser tests could not be started on the authoring machine (the Java HTTP client of that PC fails with `Unable to establish loopback connection`; this is a local problem, not a code problem). The committed reports are therefore the output of the **GitHub Actions run** (headless Chrome on ubuntu-latest, no retries), published by the workflow itself when the commit message contains `[publish-reports]`. The `Environment` column of every CSV says where the run happened.
