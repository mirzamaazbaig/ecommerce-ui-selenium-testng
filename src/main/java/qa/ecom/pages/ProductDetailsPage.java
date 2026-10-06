package qa.ecom.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductDetailsPage extends BasePage {

    private static final By NAME = By.cssSelector("h2");
    private static final By PRICE = By.cssSelector("h4.text-muted");
    private static final By QUANTITY = By.cssSelector("input[type='number']");
    private static final By ADD_TO_CART = By.xpath("//button[normalize-space()='Add to Cart']");
    private static final By ADD_TO_WISHLIST = By.xpath("//button[contains(normalize-space(),'Wishlist')]");

    public ProductDetailsPage(WebDriver driver) {
        super(driver);
    }

    public ProductDetailsPage waitUntilLoaded() {
        waitUntil("product details to load", d -> currentPath().startsWith("/products/")
                && !d.findElements(NAME).isEmpty() && !d.findElements(ADD_TO_CART).isEmpty());
        return this;
    }

    public String name() {
        return textOf(NAME);
    }

    public double price() {
        return Double.parseDouble(textOf(PRICE).replace("$", "").trim());
    }

    public ProductDetailsPage setQuantity(int quantity) {
        type(QUANTITY, String.valueOf(quantity));
        return this;
    }

    /** Adds to the cart and accepts the confirmation alert; returns the alert text. */
    public String addToCart() {
        click(ADD_TO_CART);
        return acceptAlert();
    }

    /** Adds to the wishlist and accepts the confirmation alert; returns the alert text. */
    public String addToWishlist() {
        click(ADD_TO_WISHLIST);
        return acceptAlert();
    }
}
