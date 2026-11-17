package com.allahditta.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class InventoryPage extends BasePage {
    private final By title = By.cssSelector("[data-test='title']");
    private final By items = By.cssSelector("[data-test='inventory-item']");
    private final By prices = By.cssSelector("[data-test='inventory-item-price']");
    private final By sort = By.cssSelector("[data-test='product-sort-container']");
    private final By cartBadge = By.cssSelector("[data-test='shopping-cart-badge']");
    private final By cartLink = By.cssSelector("[data-test='shopping-cart-link']");

    public InventoryPage(WebDriver driver) { super(driver); }

    public String title() { return text(title); }

    public int itemCount() { return driver.findElements(items).size(); }

    public InventoryPage addToCart(String productSlug) {
        click(By.cssSelector("[data-test='add-to-cart-" + productSlug + "']"));
        return this;
    }

    public int cartCount() {
        List<?> badge = driver.findElements(cartBadge);
        return badge.isEmpty() ? 0 : Integer.parseInt(text(cartBadge));
    }

    public InventoryPage sortBy(String visibleText) {
        new Select(visible(sort)).selectByVisibleText(visibleText);
        return this;
    }

    public List<Double> priceList() {
        return driver.findElements(prices).stream()
                .map(e -> Double.parseDouble(e.getText().replace("$", ""))).toList();
    }

    public CartPage openCart() { click(cartLink); return new CartPage(driver); }
}
