package qa.ecom.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import qa.ecom.config.Config;

import java.io.File;
import java.util.Map;

/**
 * Creates and owns one WebDriver per thread, so test classes can run in parallel without sharing a browser.
 */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static WebDriver start() {
        ChromeOptions options = new ChromeOptions();
        if (Config.headless()) {
            options.addArguments("--headless=new");
        }
        if (Config.noSandbox()) {
            options.addArguments("--no-sandbox");
        }
        options.addArguments("--window-size=1400,900", "--disable-dev-shm-usage", "--disable-gpu");
        // Keep Chrome's own password and "save address" prompts out of the way
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false));
        Config.chromeBinary().ifPresent(options::setBinary);

        // No implicit wait on purpose: every wait is an explicit, named condition (see BasePage)
        ChromeDriver driver = Config.chromedriverPath()
                .map(path -> new ChromeDriver(
                        new ChromeDriverService.Builder().usingDriverExecutable(new File(path)).build(), options))
                .orElseGet(() -> new ChromeDriver(options));

        DRIVER.set(driver);
        return driver;
    }

    public static WebDriver get() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No WebDriver for this thread; call DriverFactory.start() first");
        }
        return driver;
    }

    public static boolean isStarted() {
        return DRIVER.get() != null;
    }

    public static void quit() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
            }
        }
    }
}
