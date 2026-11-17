package com.allahditta.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    private static final String URL = "https://www.saucedemo.com";
    private final By username = By.id("user-name");
    private final By password = By.id("password");
    private final By loginButton = By.id("login-button");
    private final By error = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) { super(driver); }

    public LoginPage open() { driver.get(URL); return this; }

    public InventoryPage loginAs(String user, String pass) {
        submit(user, pass);
        return new InventoryPage(driver);
    }

    public LoginPage submit(String user, String pass) {
        type(username, user);
        type(password, pass);
        click(loginButton);
        return this;
    }

    public String errorMessage() { return text(error); }
}
