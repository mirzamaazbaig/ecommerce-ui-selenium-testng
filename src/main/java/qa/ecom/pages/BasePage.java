package qa.ecom.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import qa.ecom.config.Config;

import java.util.List;

/**
 * Shared behaviour for page objects: explicit waits only (no Thread.sleep, no implicit wait)
 * and small helpers so that page classes read as user actions.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        // React re-renders lists while we read them; a stale element just means "look again"
        this.wait = new WebDriverWait(driver, Config.timeout());
        this.wait.ignoring(StaleElementReferenceException.class);
    }

    public NavBar nav() {
        return new NavBar(driver);
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> allVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    /**
     * Clicks the element once it is clickable. Images and fonts can still be loading and shift the layout
     * between scrolling and clicking, so an intercepted click is retried (with the element re-scrolled
     * into view) until the normal timeout instead of failing on the first attempt.
     */
    protected void click(By locator) {
        wait.withMessage("to click " + locator)
                .ignoring(ElementClickInterceptedException.class, StaleElementReferenceException.class)
                .until(d -> {
                    WebElement element = d.findElement(locator);
                    ((JavascriptExecutor) d).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                    wait.until(ExpectedConditions.elementToBeClickable(element));
                    element.click();
                    return true;
                });
    }

    protected void type(By locator, String text) {
        WebElement field = visible(locator);
        field.clear();
        field.sendKeys(text);
    }

    protected String textOf(By locator) {
        return visible(locator).getText().trim();
    }

    protected boolean isDisplayed(By locator) {
        return !driver.findElements(locator).isEmpty() && driver.findElement(locator).isDisplayed();
    }

    /** Waits until the condition holds, with a readable failure message. */
    protected void waitUntil(String description, java.util.function.Function<WebDriver, Boolean> condition) {
        wait.withMessage(description).until(condition);
    }

    /** The app confirms several actions with a native alert; wait for it, read it and accept it. */
    protected String acceptAlert() {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String message = alert.getText();
        alert.accept();
        return message;
    }

    public String currentPath() {
        return java.net.URI.create(driver.getCurrentUrl()).getPath();
    }

    protected void waitForPath(String path) {
        wait.withMessage("URL path to become " + path).until(d -> currentPath().equals(path));
    }
}
