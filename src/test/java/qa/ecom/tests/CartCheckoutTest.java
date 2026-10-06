package qa.ecom.tests;

import org.testng.annotations.Test;
import qa.ecom.api.ApiClient.Product;
import qa.ecom.base.BaseTest;
import qa.ecom.pages.CartPage;
import qa.ecom.pages.HomePage;
import qa.ecom.pages.OrdersPage;
import qa.ecom.pages.ProductDetailsPage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class CartCheckoutTest extends BaseTest {

    private List<Product> catalogue() {
        return api.products();
    }

    @Test(groups = "smoke", description = "Adding a product from the home page puts it in the cart")
    public void addingFromHomePageAddsToCart() {
        Product product = catalogue().get(0);
        HomePage home = shopAsNewUser();

        home.addToCart(product.name());
        home.nav().waitForCartCount(1).openCart();

        CartPage cart = new CartPage(driver).waitUntilLoaded();
        assertThat(cart.itemNames()).containsExactly(product.name());
        assertThat(cart.quantityOf(product.name())).isEqualTo(1);
    }

    @Test(description = "A quantity chosen on the details page is carried into the cart with the right line total")
    public void quantityFromDetailsPageIsCarriedToCart() {
        Product product = catalogue().get(0);
        HomePage home = shopAsNewUser();

        ProductDetailsPage details = home.openDetails(product.name()).setQuantity(3);
        String alert = details.addToCart();
        details.nav().openCart();

        CartPage cart = new CartPage(driver).waitUntilLoaded();
        assertThat(alert).contains(product.name());
        assertThat(cart.quantityOf(product.name())).isEqualTo(3);
        assertThat(cart.lineTotal(product.name())).isCloseTo(product.price() * 3, within(0.01));
    }

    @Test(description = "Removing the only item leaves an empty cart")
    public void removingTheOnlyItemEmptiesTheCart() {
        Product product = catalogue().get(0);
        HomePage home = shopAsNewUser();
        home.addToCart(product.name());
        home.nav().waitForCartCount(1).openCart();

        CartPage cart = new CartPage(driver).waitUntilLoaded().remove(product.name());

        assertThat(cart.isEmpty()).isTrue();
    }

    @Test(description = "The cart total is the sum of the line totals, which match catalogue prices")
    public void cartTotalIsTheSumOfItsLines() {
        List<Product> products = catalogue();
        Product first = products.get(0);
        Product second = products.get(1);
        HomePage home = shopAsNewUser();
        home.addToCart(first.name());
        home.nav().waitForCartCount(1);
        home.addToCart(second.name());
        home.nav().waitForCartCount(2).openCart();

        CartPage cart = new CartPage(driver).waitUntilLoaded();

        assertThat(cart.itemNames()).containsExactlyInAnyOrder(first.name(), second.name());
        assertThat(cart.total()).isCloseTo(first.price() + second.price(), within(0.01));
    }

    @Test(groups = "smoke", description = "Checking out creates a pending order and empties the cart")
    public void checkoutCreatesOrderAndEmptiesCart() {
        Product product = catalogue().get(0);
        HomePage home = shopAsNewUser();
        home.addToCart(product.name());
        home.nav().waitForCartCount(1).openCart();

        OrdersPage orders = new CartPage(driver).waitUntilLoaded().checkout();

        assertThat(orders.orderCount()).isEqualTo(1);
        assertThat(orders.orderSummaries().get(0)).contains("PENDING").contains(String.format("$%.2f", product.price()));
        assertThat(orders.firstOrderDetails()).contains(product.name());

        orders.nav().openCart();
        assertThat(new CartPage(driver).waitUntilLoaded().isEmpty()).isTrue();
    }

    @Test(description = "A user who has not ordered sees an empty order history")
    public void newUserHasNoOrders() {
        shopAsNewUser();

        OrdersPage orders = new OrdersPage(driver).open();

        assertThat(orders.noOrdersMessageShown()).isTrue();
    }
}
