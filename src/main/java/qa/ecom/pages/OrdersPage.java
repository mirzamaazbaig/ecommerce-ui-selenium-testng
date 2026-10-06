package qa.ecom.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import qa.ecom.config.Config;

import java.util.List;

public class OrdersPage extends BasePage {

    private static final By HEADING = By.xpath("//h2[normalize-space()='My Orders']");
    private static final By NO_ORDERS = By.xpath("//h2[normalize-space()='No orders found']");
    private static final By ORDER = By.cssSelector(".accordion-item");
    private static final By ORDER_TITLE = By.cssSelector(".accordion-button");

    public OrdersPage(WebDriver driver) {
        super(driver);
    }

    public OrdersPage open() {
        driver.get(Config.baseUrl() + "/my-orders");
        return waitUntilLoaded();
    }

    public OrdersPage waitUntilLoaded() {
        waitUntil("orders page to load", d -> !d.findElements(HEADING).isEmpty() || !d.findElements(NO_ORDERS).isEmpty());
        return this;
    }

    public int orderCount() {
        return driver.findElements(ORDER).size();
    }

    public boolean noOrdersMessageShown() {
        return isDisplayed(NO_ORDERS);
    }

    /** The header text of each order, e.g. "Order #12 ... PENDING $45.00". */
    public List<String> orderSummaries() {
        return driver.findElements(ORDER_TITLE).stream().map(e -> e.getText().replaceAll("\\s+", " ").trim()).toList();
    }

    /** Text of the expanded order body (the app expands the newest order by default). */
    public String firstOrderDetails() {
        return textOf(By.cssSelector(".accordion-collapse.show .accordion-body"));
    }
}
