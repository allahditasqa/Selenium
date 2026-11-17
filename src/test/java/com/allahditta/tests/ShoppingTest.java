package com.allahditta.tests;

import com.allahditta.pages.CartPage;
import com.allahditta.pages.CheckoutPage;
import com.allahditta.pages.InventoryPage;
import com.allahditta.pages.LoginPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class ShoppingTest extends BaseTest {
    private InventoryPage inventory;

    @BeforeMethod(dependsOnMethods = "setUp", alwaysRun = true)
    public void login() {
        inventory = new LoginPage(driver).open().loginAs("standard_user", "secret_sauce");
    }

    @Test(description = "Adding items updates the cart badge")
    public void addToCartUpdatesBadge() {
        inventory.addToCart("sauce-labs-backpack").addToCart("sauce-labs-bike-light");
        assertEquals(inventory.cartCount(), 2);
    }

    @Test(description = "Price low-to-high sorting works")
    public void sortByPriceAscending() {
        List<Double> prices = inventory.sortBy("Price (low to high)").priceList();
        List<Double> sorted = new ArrayList<>(prices);
        Collections.sort(sorted);
        assertEquals(prices, sorted);
    }

    @Test(description = "Cart shows the added product")
    public void cartContainsProduct() {
        CartPage cart = inventory.addToCart("sauce-labs-backpack").openCart();
        assertEquals(cart.itemNames(), List.of("Sauce Labs Backpack"));
    }

    @Test(description = "End-to-end checkout")
    public void endToEndCheckout() {
        CheckoutPage checkout = inventory.addToCart("sauce-labs-onesie").openCart().checkout();
        checkout.fillInfo("Allah", "Ditta", "54000").finish();
        assertEquals(checkout.confirmationHeader(), "Thank you for your order!");
    }
}
