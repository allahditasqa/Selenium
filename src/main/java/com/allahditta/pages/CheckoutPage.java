package com.allahditta.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckoutPage extends BasePage {
    public CheckoutPage(WebDriver driver) { super(driver); }

    public CheckoutPage fillInfo(String first, String last, String zip) {
        type(By.id("first-name"), first);
        type(By.id("last-name"), last);
        type(By.id("postal-code"), zip);
        click(By.id("continue"));
        return this;
    }

    public CheckoutPage finish() { click(By.id("finish")); return this; }

    public String confirmationHeader() { return text(By.cssSelector("[data-test='complete-header']")); }
}
