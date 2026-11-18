package com.allahditta.tests;

import com.allahditta.pages.InventoryPage;
import com.allahditta.pages.LoginPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class LoginTest extends BaseTest {

    @Test(description = "TC_LOGIN_01 - Login with username \"standard_user\" and password \"secret_sauce\"")
    public void validLogin() {
        InventoryPage inventory = new LoginPage(driver).open().loginAs("standard_user", "secret_sauce");
        assertEquals(inventory.title(), "Products");
        assertEquals(inventory.itemCount(), 6);
    }

    @Test(description = "TC_LOGIN_02 - Login with the locked out user \"locked_out_user\" and password \"secret_sauce\"")
    public void lockedOutUser() {
        LoginPage page = new LoginPage(driver).open().submit("locked_out_user", "secret_sauce");
        assertTrue(page.errorMessage().contains("locked out"));
    }

    /** The first value is the test case ID (TC_LOGIN_03 .. TC_LOGIN_06), read by the report listener. */
    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][]{
                {"TC_LOGIN_03", "standard_user", "wrong_password", "Username and password do not match"},
                {"TC_LOGIN_04", "unknown_user", "secret_sauce", "Username and password do not match"},
                {"TC_LOGIN_05", "", "secret_sauce", "Username is required"},
                {"TC_LOGIN_06", "standard_user", "", "Password is required"},
        };
    }

    @Test(dataProvider = "invalidCredentials", description = "TC_LOGIN_03..06 - Invalid credentials are rejected with the correct message")
    public void invalidLogin(String testCaseId, String user, String pass, String expectedError) {
        LoginPage page = new LoginPage(driver).open().submit(user, pass);
        assertTrue(page.errorMessage().contains(expectedError), page.errorMessage());
    }
}
