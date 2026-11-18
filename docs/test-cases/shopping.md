# Shopping - Test Cases

**Module:** Shopping
**Automation:** `src/test/java/com/allahditta/tests/ShoppingTest.java`  |  **Report:** `reports/shopping/Shopping test <DD-MM-YYYY>_<HH-mm-ss>.html` (all reports: `reports/index.html`)
**Page:** https://www.saucedemo.com

## Overview
Products, cart and checkout of the Swag Labs demo shop, written with the Page Object Model (`InventoryPage`, `CartPage`, `CheckoutPage`).

## Scope and notes
- Every case logs in first as "standard_user" with the password "secret_sauce" and starts with an empty cart (fresh browser).
- The demo shop keeps the cart in the browser, so nothing needs to be cleaned up.

## A. Products

| ID | Title | Priority | Type | Preconditions | Test data | Steps | Expected result | Automated in |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TC_SHOPPING_01 | Add the products "Sauce Labs Backpack" and "Sauce Labs Bike Light" and check the cart badge shows 2 | High | Functional | User "standard_user" is logged in, cart is empty | Products Sauce Labs Backpack and Sauce Labs Bike Light | 1. Click Add to cart on "Sauce Labs Backpack". 2. Click Add to cart on "Sauce Labs Bike Light". | The cart badge shows 2. | `src/test/java/com/allahditta/tests/ShoppingTest.java::addToCartUpdatesBadge` |
| TC_SHOPPING_02 | Sort the products by "Price (low to high)" and check the prices are in ascending order | Medium | Functional | User "standard_user" is logged in | Sort option "Price (low to high)" | 1. Choose "Price (low to high)" in the sort list. 2. Read all product prices. | The prices are in ascending order (each price is equal to or higher than the one before). | `src/test/java/com/allahditta/tests/ShoppingTest.java::sortByPriceAscending` |

## B. Cart and checkout

| ID | Title | Priority | Type | Preconditions | Test data | Steps | Expected result | Automated in |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TC_SHOPPING_03 | Add "Sauce Labs Backpack", open the cart and check it contains only that product | High | Functional | User "standard_user" is logged in, cart is empty | Product Sauce Labs Backpack | 1. Click Add to cart on "Sauce Labs Backpack". 2. Open the cart. | The cart lists exactly one product: "Sauce Labs Backpack". | `src/test/java/com/allahditta/tests/ShoppingTest.java::cartContainsProduct` |
| TC_SHOPPING_04 | Buy "Sauce Labs Onesie" with first name "Allah", last name "Ditta" and zip code "54000" and check the order is confirmed | High | End to end | User "standard_user" is logged in, cart is empty | Product Sauce Labs Onesie; first name Allah, last name Ditta, zip code 54000 | 1. Click Add to cart on "Sauce Labs Onesie". 2. Open the cart and click Checkout. 3. Enter the first name, last name and zip code and click Continue. 4. Click Finish. | The confirmation "Thank you for your order!" is shown. | `src/test/java/com/allahditta/tests/ShoppingTest.java::endToEndCheckout` |

## Summary

| Cases | High priority | Automated |
| --- | --- | --- |
| 4 | 3 | 4 |
