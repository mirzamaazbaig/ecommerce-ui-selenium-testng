package qa.ecom.base;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import qa.ecom.api.ApiClient;
import qa.ecom.config.Config;
import qa.ecom.data.TestUser;
import qa.ecom.driver.DriverFactory;
import qa.ecom.pages.HomePage;

import java.net.URI;

/** Starts a fresh browser for every test and provides fast, API-based ways to reach a logged-in state. */
public abstract class BaseTest {

    protected WebDriver driver;
    protected final ApiClient api = new ApiClient();

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        driver = DriverFactory.start();
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser() {
        DriverFactory.quit();
    }

    /**
     * Creates a new customer through the API and signs the browser in by handing it the session cookie,
     * so tests that are not about logging in do not repeat the login screen. Returns the user.
     */
    protected TestUser signedInUser() {
        TestUser user = TestUser.unique();
        String sessionCookie = api.register(user);

        // A cookie can only be set for the site that is currently open
        driver.get(Config.baseUrl() + "/login");
        driver.manage().addCookie(new Cookie.Builder(ApiClient.SESSION_COOKIE, sessionCookie)
                .domain(URI.create(Config.baseUrl()).getHost())
                .path("/")
                .isHttpOnly(true)
                .build());
        return user;
    }

    /** Signs in through the API (see signedInUser) and opens the home page, ready to shop. */
    protected HomePage shopAsNewUser() {
        signedInUser();
        HomePage home = new HomePage(driver).open();
        home.nav().waitUntilLoggedIn();
        return home;
    }
}
