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

    @Test(description = "TC_SHOPPING_01 - Add the products \"Sauce Labs Backpack\" and \"Sauce Labs Bike Light\" and check the cart badge shows 2")
    public void addToCartUpdatesBadge() {
        inventory.addToCart("sauce-labs-backpack").addToCart("sauce-labs-bike-light");
        assertEquals(inventory.cartCount(), 2);
    }

    @Test(description = "TC_SHOPPING_02 - Sort the products by \"Price (low to high)\" and check the prices are in ascending order")
    public void sortByPriceAscending() {
        List<Double> prices = inventory.sortBy("Price (low to high)").priceList();
        List<Double> sorted = new ArrayList<>(prices);
        Collections.sort(sorted);
        assertEquals(prices, sorted);
    }

    @Test(description = "TC_SHOPPING_03 - Add \"Sauce Labs Backpack\", open the cart and check it contains only that product")
    public void cartContainsProduct() {
        CartPage cart = inventory.addToCart("sauce-labs-backpack").openCart();
        assertEquals(cart.itemNames(), List.of("Sauce Labs Backpack"));
    }

    @Test(description = "TC_SHOPPING_04 - Buy \"Sauce Labs Onesie\" with first name \"Allah\", last name \"Ditta\" and zip code \"54000\" and check the order is confirmed")
    public void endToEndCheckout() {
        CheckoutPage checkout = inventory.addToCart("sauce-labs-onesie").openCart().checkout();
        checkout.fillInfo("Allah", "Ditta", "54000").finish();
        assertEquals(checkout.confirmationHeader(), "Thank you for your order!");
    }
}
