package qa.ecom.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import qa.ecom.config.Config;

import java.util.List;

public class CartPage extends BasePage {

    private static final By HEADING = By.xpath("//h2[normalize-space()='Shopping Cart']");
    private static final By EMPTY_HEADING = By.xpath("//h2[normalize-space()='Your Cart is Empty']");
    private static final By LINE_ITEM = By.cssSelector("ul.list-group.mb-3 > li.list-group-item.lh-sm");
    private static final By CHECKOUT = By.xpath("//button[normalize-space()='Checkout']");
    private static final By TOTAL = By.xpath("//li[.//span[normalize-space()='Total (USD)']]//strong");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public CartPage open() {
        driver.get(Config.baseUrl() + "/cart");
        waitUntilLoaded();
        return this;
    }

    public CartPage waitUntilLoaded() {
        waitUntil("cart page to load", d -> !d.findElements(HEADING).isEmpty() || !d.findElements(EMPTY_HEADING).isEmpty());
        return this;
    }

    public boolean isEmpty() {
        return isDisplayed(EMPTY_HEADING);
    }

    public List<String> itemNames() {
        return driver.findElements(LINE_ITEM).stream()
                .map(li -> li.findElement(By.cssSelector("h6")).getText().trim())
                .toList();
    }

    public int quantityOf(String productName) {
        String text = line(productName).findElement(By.cssSelector("small")).getText();   // "Quantity: 2"
        return Integer.parseInt(text.replaceAll("\\D+", ""));
    }

    public double lineTotal(String productName) {
        String text = line(productName).findElement(By.cssSelector(".text-end span")).getText();
        return Double.parseDouble(text.replace("$", "").replace(",", ""));
    }

    public double total() {
        return Double.parseDouble(textOf(TOTAL).replace("$", "").replace(",", ""));
    }

    public CartPage remove(String productName) {
        line(productName).findElement(By.xpath(".//button[normalize-space()='Remove']")).click();
        return this;
    }

    /** Checks out, accepts the confirmation alert and expects to land on the orders page. */
    public OrdersPage checkout() {
        click(CHECKOUT);
        acceptAlert();
        waitForPath("/my-orders");
        return new OrdersPage(driver).waitUntilLoaded();
    }

    private WebElement line(String productName) {
        return driver.findElements(LINE_ITEM).stream()
                .filter(li -> li.findElement(By.cssSelector("h6")).getText().trim().equals(productName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("'" + productName + "' is not in the cart; cart has " + itemNames()));
    }
}
