package qa.ecom.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
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

    /**
     * Waits until the page's finite animations have finished. Pages fade in over about 0.7 s, and while an element
     * is still transparent Selenium reports its text as empty, so a read at that moment returns "" and the test
     * fails intermittently. Endless animations (a loading spinner) are ignored.
     */
    protected void waitForAnimations() {
        wait.withMessage("page animations to finish").until(d -> (Boolean) ((JavascriptExecutor) d).executeScript(
                "return document.getAnimations().every(a => a.playState !== 'running' "
                        + "|| a.effect.getComputedTiming().iterations === Infinity);"));
    }

    protected WebElement visible(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        waitForAnimations();
        return element;
    }

    protected List<WebElement> allVisible(By locator) {
        List<WebElement> elements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
        waitForAnimations();
        return elements;
    }

    /**
     * Clicks the element once it is clickable. Images and fonts can still be loading and shift the layout
     * between scrolling and clicking, so an intercepted click is retried (with the element re-scrolled
     * into view) until the normal timeout instead of failing on the first attempt.
     */
    protected void click(By locator) {
        try {
            wait.withMessage("to click " + locator)
                    .ignoring(ElementClickInterceptedException.class, StaleElementReferenceException.class)
                    .until(d -> {
                        waitForAnimations();
                        WebElement element = d.findElement(locator);
                        ((JavascriptExecutor) d).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                        wait.until(ExpectedConditions.elementToBeClickable(element));
                        element.click();
                        return true;
                    });
        } catch (TimeoutException e) {
            // Say what is in the way, so a blocked click can be diagnosed from the log alone
            throw new TimeoutException(e.getMessage() + "\nElement at the click point: " + elementAtCenterOf(locator), e);
        }
    }

    /** Describes whatever the browser reports at the centre of the element (the element that would get the click). */
    private String elementAtCenterOf(By locator) {
        try {
            Object described = ((JavascriptExecutor) driver).executeScript(
                    "const r = arguments[0].getBoundingClientRect();"
                            + "const x = r.left + r.width / 2, y = r.top + r.height / 2;"
                            + "const top = document.elementFromPoint(x, y);"
                            + "return 'viewport ' + innerWidth + 'x' + innerHeight + ', point (' + Math.round(x) + ',' + Math.round(y) + '), '"
                            + " + (top ? top.outerHTML.slice(0, 200) : 'nothing (outside the viewport)');",
                    driver.findElement(locator));
            return String.valueOf(described);
        } catch (RuntimeException ignored) {
            return "(could not be determined)";
        }
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
