# Login - Test Cases

**Module:** Login
**Automation:** `src/test/java/com/allahditta/tests/LoginTest.java`  |  **Report:** `reports/login/Login test <DD-MM-YYYY>_<HH-mm-ss>.html` (all reports: `reports/index.html`)
**Page:** https://www.saucedemo.com

## Overview
The login page of the Swag Labs demo shop lets a user sign in with a username and a password. After a successful login the user sees the "Products" page with 6 products.

## Scope and notes
- Demo users and the password are printed on the login page itself (public): `standard_user`, `locked_out_user`, password `secret_sauce`.
- Every case starts with a fresh browser (the user is logged out).
- TC_LOGIN_03 to TC_LOGIN_06 are one data-driven test (four data rows); the first value of each data row is the test case ID.

## A. Positive

| ID | Title | Priority | Type | Preconditions | Test data | Steps | Expected result | Automated in |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TC_LOGIN_01 | Login with username "standard_user" and password "secret_sauce" | High | Functional | User is logged out | Username standard_user, password secret_sauce | 1. Open the login page. 2. Enter the username and the password. 3. Click Login. | The "Products" page opens and it shows 6 products. | `src/test/java/com/allahditta/tests/LoginTest.java::validLogin` |

## B. Negative

| ID | Title | Priority | Type | Preconditions | Test data | Steps | Expected result | Automated in |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| TC_LOGIN_02 | Login with the locked out user "locked_out_user" and password "secret_sauce" | High | Negative | User is logged out | Username locked_out_user, password secret_sauce | 1. Open the login page. 2. Enter the username and the password. 3. Click Login. | An error message that says the user has been locked out is shown. The user stays on the login page. | `src/test/java/com/allahditta/tests/LoginTest.java::lockedOutUser` |
| TC_LOGIN_03 | Login with username "standard_user" and the wrong password "wrong_password" | High | Negative | User is logged out | Username standard_user, password wrong_password | 1. Open the login page. 2. Enter the username and the password. 3. Click Login. | The error "Username and password do not match any user in this service" is shown. | `src/test/java/com/allahditta/tests/LoginTest.java::invalidLogin` |
| TC_LOGIN_04 | Login with the unknown username "unknown_user" and password "secret_sauce" | Medium | Negative | User is logged out | Username unknown_user, password secret_sauce | 1. Open the login page. 2. Enter the username and the password. 3. Click Login. | The error "Username and password do not match any user in this service" is shown. | `src/test/java/com/allahditta/tests/LoginTest.java::invalidLogin` |
| TC_LOGIN_05 | Login with an empty username and password "secret_sauce" | Medium | Negative | User is logged out | Username empty, password secret_sauce | 1. Open the login page. 2. Leave the username empty and enter the password. 3. Click Login. | The error "Username is required" is shown. | `src/test/java/com/allahditta/tests/LoginTest.java::invalidLogin` |
| TC_LOGIN_06 | Login with username "standard_user" and an empty password | Medium | Negative | User is logged out | Username standard_user, password empty | 1. Open the login page. 2. Enter the username and leave the password empty. 3. Click Login. | The error "Password is required" is shown. | `src/test/java/com/allahditta/tests/LoginTest.java::invalidLogin` |

## Summary

| Cases | High priority | Automated |
| --- | --- | --- |
| 6 | 3 | 6 |
