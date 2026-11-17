package com.allahditta.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

public class CartPage extends BasePage {
    private final By itemNames = By.cssSelector("[data-test='inventory-item-name']");
    private final By checkout = By.cssSelector("[data-test='checkout']");

    public CartPage(WebDriver driver) { super(driver); }

    public List<String> itemNames() {
        visible(By.cssSelector("[data-test='cart-list']"));
        return driver.findElements(itemNames).stream().map(e -> e.getText()).toList();
    }

    public CheckoutPage checkout() { click(checkout); return new CheckoutPage(driver); }
}
