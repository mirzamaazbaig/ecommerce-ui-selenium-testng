package qa.ecom.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import qa.ecom.config.Config;
import qa.ecom.data.TestUser;

public class LoginPage extends BasePage {

    private static final By HEADING = By.xpath("//h2[normalize-space()='Login']");
    private static final By EMAIL = By.id("email");
    private static final By PASSWORD = By.id("password");
    private static final By SUBMIT = By.xpath("//form//button[@type='submit'][normalize-space()='Login']");
    private static final By ERROR = By.cssSelector(".alert-danger");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(Config.baseUrl() + "/login");
        visible(HEADING);
        return this;
    }

    /** Submits the form and expects to land on the home page, logged in. */
    public HomePage loginAs(TestUser user) {
        submit(user.email(), user.password());
        waitForPath("/");
        nav().waitUntilLoggedIn();
        return new HomePage(driver);
    }

    /** Submits the form and stays on the login page; use errorMessage() to read the result. */
    public LoginPage submit(String email, String password) {
        type(EMAIL, email);
        type(PASSWORD, password);
        click(SUBMIT);
        return this;
    }

    /** Waits for the redirect to the login screen and returns the resulting path. */
    public String currentPathAfterRedirect() {
        visible(HEADING);
        return currentPath();
    }

    public String errorMessage() {
        return textOf(ERROR);
    }
}
