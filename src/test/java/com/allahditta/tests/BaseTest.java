package com.allahditta.tests;

import com.allahditta.report.ReportListener;
import com.allahditta.utils.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.HasCdp;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.Map;

public abstract class BaseTest {
    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.create();
        // lets the report listener take a screenshot of this browser when the test ends
        ReportListener.SCREENSHOT.set(this::screenshotDataUri);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        ReportListener.SCREENSHOT.remove();
        if (driver != null) driver.quit();
    }

    /** JPEG through the DevTools protocol (small); falls back to PNG for browsers without it. */
    private String screenshotDataUri() {
        if (driver instanceof HasCdp cdp) {
            try {
                Object data = cdp.executeCdpCommand("Page.captureScreenshot", Map.of("format", "jpeg", "quality", 55)).get("data");
                return "data:image/jpeg;base64," + data;
            } catch (RuntimeException ignored) {
                // fall through to the standard screenshot
            }
        }
        return "data:image/png;base64," + ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }
}
