package qa.ecom.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import qa.ecom.config.Config;

import java.util.List;
import java.util.function.Predicate;

/** The product listing with its category, sort and search controls. */
public class HomePage extends BasePage {

    private static final By CARD = By.cssSelector(".card");
    private static final By NO_RESULTS = By.xpath("//p[normalize-space()='No products found.']");
    private static final By RESULTS_HEADING = By.xpath("//h3[starts-with(normalize-space(),'Results for')]");
    private static final By SORT_SELECT = By.xpath(
            "//div[contains(@class,'filter-section')][.//h5[normalize-space()='Sort By']]//select");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public HomePage open() {
        driver.get(Config.baseUrl() + "/");
        waitUntilLoaded();
        return this;
    }

    /** The grid is ready once there are product cards or the "no products" message. */
    public HomePage waitUntilLoaded() {
        waitUntil("product list to load", d -> !d.findElements(CARD).isEmpty() || !d.findElements(NO_RESULTS).isEmpty());
        return this;
    }

    /**
     * Product names in display order. Read in one script call so a re-render of the grid in the middle
     * of reading cannot leave us holding stale elements.
     */
    @SuppressWarnings("unchecked")
    public List<String> productNames() {
        return (List<String>) ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('.card .card-title')).map(e => e.getAttribute('title'));");
    }

    /** Prices as numbers, in display order. */
    @SuppressWarnings("unchecked")
    public List<Double> productPrices() {
        List<String> texts = (List<String>) ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('.card .card-text.fw-bold')).map(e => e.textContent);");
        return texts.stream().map(t -> Double.parseDouble(t.replace("$", "").replace(",", "").trim())).toList();
    }

    /**
     * The list is re-fetched after every filter, sort or search while the old cards stay on screen,
     * so tests wait for the names they expect instead of reading the grid straight away.
     */
    public HomePage waitForProductNames(String description, Predicate<List<String>> condition) {
        waitUntil(description, d -> condition.test(productNames()));
        return this;
    }

    public HomePage waitForPrices(String description, Predicate<List<Double>> condition) {
        waitUntil(description, d -> condition.test(productPrices()));
        return this;
    }

    public HomePage waitForNoProducts() {
        waitUntil("the 'No products found.' message", d -> !d.findElements(NO_RESULTS).isEmpty());
        return this;
    }

    public HomePage selectCategory(String category) {
        click(By.xpath("//li[normalize-space()='" + category + "']"));
        return this;
    }

    public HomePage sortBy(String optionText) {
        new Select(visible(SORT_SELECT)).selectByVisibleText(optionText);
        return this;
    }

    public HomePage search(String term) {
        nav().search(term);
        return this;
    }

    public String resultsHeading() {
        return textOf(RESULTS_HEADING);
    }

    public boolean noProductsMessageShown() {
        return isDisplayed(NO_RESULTS);
    }

    public HomePage addToCart(String productName) {
        click(addToCartButton(productName));
        return this;
    }

    public ProductDetailsPage openDetails(String productName) {
        click(detailsLink(productName));
        return new ProductDetailsPage(driver).waitUntilLoaded();
    }

    /** The "Add to Cart" button inside the card of the named product. */
    private By addToCartButton(String productName) {
        return By.xpath(cardXpath(productName) + "//button[contains(@class,'btn-primary')]");
    }

    /** The "View Details" link inside the card of the named product. */
    private By detailsLink(String productName) {
        return By.xpath(cardXpath(productName) + "//a[contains(@class,'btn-outline-secondary')]");
    }

    private static String cardXpath(String productName) {
        return "//div[contains(@class,'card')][.//h5[@title=" + xpathLiteral(productName) + "]]";
    }

    static String xpathLiteral(String text) {
        if (!text.contains("'")) {
            return "'" + text + "'";
        }
        return "concat('" + text.replace("'", "', \"'\", '") + "')";
    }

    public List<WebElement> cards() {
        return driver.findElements(CARD);
    }
}
