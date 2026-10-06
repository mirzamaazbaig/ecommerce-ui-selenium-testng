package qa.ecom.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** The top navigation bar, present on every page. */
public class NavBar extends BasePage {

    private static final By SEARCH_INPUT = By.cssSelector("input[placeholder='Search products...']");
    private static final By SEARCH_BUTTON = By.cssSelector(".search-btn");
    private static final By SIGN_IN_LINK = By.cssSelector("a[href='/login']");
    private static final By ACCOUNT_MENU = By.cssSelector(".nav-link.dropdown-toggle");
    private static final By LOGOUT = By.xpath("//button[contains(@class,'dropdown-item')][normalize-space()='Logout']");
    private static final By CART_LINK = By.cssSelector("a[href='/cart']");
    private static final By CART_BADGE = By.cssSelector("a[href='/cart'] .badge");
    private static final By ORDERS_LINK = By.cssSelector("a[href='/my-orders']");
    private static final By WISHLIST_LINK = By.cssSelector("a[href='/wishlist']");

    public NavBar(WebDriver driver) {
        super(driver);
    }

    public boolean isLoggedIn() {
        return isDisplayed(ACCOUNT_MENU);
    }

    public void waitUntilLoggedIn() {
        visible(ACCOUNT_MENU);
    }

    public void waitUntilLoggedOut() {
        visible(SIGN_IN_LINK);
    }

    public void logout() {
        click(ACCOUNT_MENU);
        click(LOGOUT);
        waitUntilLoggedOut();
    }

    public void search(String term) {
        type(SEARCH_INPUT, term);
        click(SEARCH_BUTTON);
    }

    public void openCart() {
        click(CART_LINK);
        waitForPath("/cart");
    }

    public void openOrders() {
        click(ORDERS_LINK);
        waitForPath("/my-orders");
    }

    public void openWishlist() {
        click(WISHLIST_LINK);
        waitForPath("/wishlist");
    }

    public NavBar waitForCartCount(int expected) {
        waitUntil("cart badge to show " + expected, d -> cartCount() == expected);
        return this;
    }

    /** The number on the cart badge, or 0 when the badge is not shown. */
    public int cartCount() {
        return driver.findElements(CART_BADGE).stream()
                .findFirst()
                .map(e -> Integer.parseInt(e.getText().trim()))
                .orElse(0);
    }
}
