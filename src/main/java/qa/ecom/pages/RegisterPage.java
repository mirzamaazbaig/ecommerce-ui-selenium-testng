package qa.ecom.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import qa.ecom.config.Config;
import qa.ecom.data.TestUser;

public class RegisterPage extends BasePage {

    private static final By HEADING = By.xpath("//h2[normalize-space()='Register']");
    private static final By EMAIL = By.id("email");
    private static final By PASSWORD = By.id("password");
    private static final By CONFIRM = By.id("confirmPassword");
    private static final By SUBMIT = By.xpath("//form//button[@type='submit'][normalize-space()='Register']");
    private static final By ERROR = By.cssSelector(".alert-danger");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public RegisterPage open() {
        driver.get(Config.baseUrl() + "/register");
        visible(HEADING);
        return this;
    }

    /** Registers and expects to land on the home page, logged in. */
    public HomePage registerAs(TestUser user) {
        submit(user.email(), user.password(), user.password());
        waitForPath("/");
        nav().waitUntilLoggedIn();
        return new HomePage(driver);
    }

    /** Submits the form and stays on the register page; use errorMessage() to read the result. */
    public RegisterPage submit(String email, String password, String confirmation) {
        type(EMAIL, email);
        type(PASSWORD, password);
        type(CONFIRM, confirmation);
        click(SUBMIT);
        return this;
    }

    public String errorMessage() {
        return textOf(ERROR);
    }
}
