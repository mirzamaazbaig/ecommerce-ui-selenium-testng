package qa.ecom.tests;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import qa.ecom.base.BaseTest;
import qa.ecom.config.Config;
import qa.ecom.data.TestUser;
import qa.ecom.pages.HomePage;
import qa.ecom.pages.LoginPage;
import qa.ecom.pages.RegisterPage;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthTest extends BaseTest {

    @Test(groups = "smoke", description = "A new user can register and is logged in on the home page")
    public void registeringCreatesAccountAndSignsIn() {
        TestUser user = TestUser.unique();

        HomePage home = new RegisterPage(driver).open().registerAs(user);

        assertThat(home.nav().isLoggedIn()).isTrue();
        assertThat(home.currentPath()).isEqualTo("/");
    }

    @Test(description = "Mismatched password confirmation is rejected on the form")
    public void registeringWithMismatchedPasswordsShowsError() {
        RegisterPage page = new RegisterPage(driver).open()
                .submit(TestUser.unique().email(), "TestPass123!", "Different123!");

        assertThat(page.errorMessage()).isEqualTo("Passwords do not match");
        assertThat(page.currentPath()).isEqualTo("/register");
    }

    @Test(description = "An email that is already registered cannot be registered again")
    public void registeringWithExistingEmailShowsError() {
        TestUser existing = TestUser.unique();
        api.register(existing);

        RegisterPage page = new RegisterPage(driver).open()
                .submit(existing.email(), existing.password(), existing.password());

        assertThat(page.errorMessage()).isEqualTo("User already exists");
        assertThat(page.currentPath()).isEqualTo("/register");
    }

    @Test(groups = "smoke", description = "A registered user can log in with the right credentials")
    public void loginWithValidCredentialsSignsIn() {
        TestUser user = TestUser.unique();
        api.register(user);

        HomePage home = new LoginPage(driver).open().loginAs(user);

        assertThat(home.nav().isLoggedIn()).isTrue();
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        TestUser known = TestUser.unique();
        api.register(known);
        return new Object[][]{
                {"wrong password", known.email(), "NotThePassword1!"},
                {"unknown email", TestUser.unique().email(), TestUser.DEFAULT_PASSWORD},
        };
    }

    @Test(dataProvider = "invalidCredentials", description = "Bad credentials are rejected with one generic message")
    public void loginWithInvalidCredentialsShowsError(String scenario, String email, String password) {
        LoginPage page = new LoginPage(driver).open().submit(email, password);

        assertThat(page.errorMessage()).as(scenario).isEqualTo("Invalid credentials");
        assertThat(page.currentPath()).isEqualTo("/login");
        assertThat(page.nav().isLoggedIn()).isFalse();
    }

    @Test(groups = "smoke", description = "Logging out returns to the signed-out navigation")
    public void logoutSignsOut() {
        HomePage home = shopAsNewUser();

        home.nav().logout();

        assertThat(home.nav().isLoggedIn()).isFalse();
    }

    @Test(description = "The session survives a full page reload")
    public void sessionPersistsAfterRefresh() {
        HomePage home = shopAsNewUser();

        driver.navigate().refresh();

        home.nav().waitUntilLoggedIn();
        assertThat(home.nav().isLoggedIn()).isTrue();
    }

    @DataProvider(name = "protectedPages")
    public Object[][] protectedPages() {
        return new Object[][]{{"/cart"}, {"/my-orders"}, {"/wishlist"}, {"/profile"}};
    }

    @Test(dataProvider = "protectedPages", description = "Anonymous visitors are sent to the login page")
    public void protectedPagesRedirectAnonymousUsersToLogin(String path) {
        driver.get(Config.baseUrl() + path);

        assertThat(new LoginPage(driver).currentPathAfterRedirect()).isEqualTo("/login");
    }
}
