package com.allahditta.tests;

import com.allahditta.pages.InventoryPage;
import com.allahditta.pages.LoginPage;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class LoginTest extends BaseTest {

    @Test(description = "Standard user can log in")
    public void validLogin() {
        InventoryPage inventory = new LoginPage(driver).open().loginAs("standard_user", "secret_sauce");
        assertEquals(inventory.title(), "Products");
        assertEquals(inventory.itemCount(), 6);
    }

    @Test(description = "Locked-out user sees an error")
    public void lockedOutUser() {
        LoginPage page = new LoginPage(driver).open().submit("locked_out_user", "secret_sauce");
        assertTrue(page.errorMessage().contains("locked out"));
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][]{
                {"standard_user", "wrong_password", "Username and password do not match"},
                {"unknown_user", "secret_sauce", "Username and password do not match"},
                {"", "secret_sauce", "Username is required"},
                {"standard_user", "", "Password is required"},
        };
    }

    @Test(dataProvider = "invalidCredentials", description = "Invalid credentials are rejected")
    public void invalidLogin(String user, String pass, String expectedError) {
        LoginPage page = new LoginPage(driver).open().submit(user, pass);
        assertTrue(page.errorMessage().contains(expectedError), page.errorMessage());
    }
}
