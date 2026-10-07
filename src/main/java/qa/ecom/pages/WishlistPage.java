package qa.ecom.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import qa.ecom.config.Config;

import java.util.List;

public class WishlistPage extends BasePage {

    private static final By HEADING = By.xpath("//h2[normalize-space()='My Wishlist']");
    private static final By EMPTY_HEADING = By.xpath("//h2[normalize-space()='Your Wishlist is Empty']");
    private static final By CARD_TITLE = By.cssSelector(".card .card-title");

    public WishlistPage(WebDriver driver) {
        super(driver);
    }

    public WishlistPage open() {
        driver.get(Config.baseUrl() + "/wishlist");
        return waitUntilLoaded();
    }

    public WishlistPage waitUntilLoaded() {
        waitUntil("wishlist to load", d -> !d.findElements(HEADING).isEmpty() || !d.findElements(EMPTY_HEADING).isEmpty());
        return this;
    }

    public boolean isEmpty() {
        return isDisplayed(EMPTY_HEADING);
    }

    public List<String> itemNames() {
        waitForAnimations();
        return driver.findElements(CARD_TITLE).stream().map(e -> e.getText().trim()).toList();
    }

    public WishlistPage remove(String productName) {
        click(button(productName, "Remove from Wishlist"));
        return this;
    }

    /** Adds to the cart from the wishlist and accepts the confirmation alert; returns the alert text. */
    public String addToCart(String productName) {
        click(button(productName, "Add to Cart"));
        return acceptAlert();
    }

    public WishlistPage waitUntilItemRemoved(String productName) {
        waitUntil(productName + " to disappear from the wishlist",
                d -> !itemNames().contains(productName));
        return this;
    }

    private static By button(String productName, String label) {
        return By.xpath("//div[contains(@class,'card')][.//h5[@title=" + HomePage.xpathLiteral(productName) + "]]"
                + "//button[normalize-space()='" + label + "']");
    }
}
