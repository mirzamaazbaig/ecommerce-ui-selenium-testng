package qa.ecom.tests;

import org.testng.annotations.Test;
import qa.ecom.api.ApiClient.Product;
import qa.ecom.base.BaseTest;
import qa.ecom.pages.HomePage;
import qa.ecom.pages.ProductDetailsPage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Browsing the catalogue. What the page shows is checked against what the API returns. */
public class ProductBrowsingTest extends BaseTest {

    private static final int ELECTRONICS = 1;

    private static List<String> names(List<Product> products) {
        return products.stream().map(Product::name).toList();
    }

    @Test(groups = "smoke", description = "The home page lists every product in the catalogue")
    public void homePageListsTheWholeCatalogue() {
        List<String> expected = names(api.products());

        HomePage home = new HomePage(driver).open();

        assertThat(expected).isNotEmpty();
        assertThat(home.productNames()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test(description = "Filtering by category shows only that category's products")
    public void categoryFilterShowsOnlyThatCategory() {
        List<String> all = names(api.products());
        List<String> electronics = names(api.products(Map.of("category_id", String.valueOf(ELECTRONICS))));
        assertThat(electronics).isNotEmpty().hasSizeLessThan(all.size());

        HomePage home = new HomePage(driver).open().selectCategory("Electronics");

        home.waitForProductNames("only the Electronics products", n -> new HashSet<>(n).equals(new HashSet<>(electronics)));
        assertThat(home.productNames()).containsExactlyInAnyOrderElementsOf(electronics);
    }

    @Test(description = "Choosing All Departments after a filter brings back the full list")
    public void allDepartmentsRestoresTheFullList() {
        List<String> all = names(api.products());
        HomePage home = new HomePage(driver).open().selectCategory("Electronics");
        home.waitForProductNames("filtered list", n -> n.size() < all.size());

        home.selectCategory("All Departments");

        home.waitForProductNames("the full list", n -> n.size() == all.size());
        assertThat(home.productNames()).containsExactlyInAnyOrderElementsOf(all);
    }

    @Test(description = "Sorting by price low to high puts the cheapest product first")
    public void sortingByPriceOrdersAscending() {
        HomePage home = new HomePage(driver).open();

        home.sortBy("Price: Low to High");

        home.waitForPrices("prices in ascending order", prices ->
                prices.size() > 1 && prices.equals(prices.stream().sorted().toList()));
        assertThat(home.productPrices()).isSorted();
    }

    @Test(groups = "smoke", description = "Searching shows a results heading and only matching products")
    public void searchShowsMatchingProducts() {
        HomePage home = new HomePage(driver).open().search("T-Shirt");

        home.waitForProductNames("only products matching 'shirt'",
                n -> !n.isEmpty() && n.stream().allMatch(name -> name.toLowerCase().contains("shirt")));
        assertThat(home.resultsHeading()).isEqualTo("Results for \"T-Shirt\"");
    }

    @Test(description = "A search without matches says so instead of showing an empty page")
    public void searchWithNoMatchShowsMessage() {
        HomePage home = new HomePage(driver).open().search("XyzNoSuchProduct12345");

        home.waitForNoProducts();
        assertThat(home.productNames()).isEmpty();
    }

    @Test(description = "A product's details page shows the same name and price as its card")
    public void detailsPageMatchesTheCatalogue() {
        Product expected = api.products().get(0);

        ProductDetailsPage details = new HomePage(driver).open().openDetails(expected.name());

        assertThat(details.name()).isEqualTo(expected.name());
        assertThat(details.price()).isEqualTo(expected.price());
    }
}
