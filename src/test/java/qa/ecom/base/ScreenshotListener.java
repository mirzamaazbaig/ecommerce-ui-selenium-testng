package qa.ecom.base;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;
import qa.ecom.driver.DriverFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Saves a screenshot to target/screenshots when a test fails, before the browser is closed. */
public class ScreenshotListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        if (!DriverFactory.isStarted()) {
            return;
        }
        try {
            byte[] png = ((TakesScreenshot) DriverFactory.get()).getScreenshotAs(OutputType.BYTES);
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            String name = result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
            Files.write(dir.resolve(name + ".png"), png);
            System.out.println("Screenshot saved: " + dir.resolve(name + ".png") + " (url: " + DriverFactory.get().getCurrentUrl() + ")");
        } catch (IOException | RuntimeException e) {
            System.out.println("Could not capture screenshot: " + e.getMessage());
        }
    }
}
