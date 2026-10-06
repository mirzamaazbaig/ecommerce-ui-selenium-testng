package qa.ecom.tests;

import org.testng.annotations.Test;
import qa.ecom.api.ApiClient.Product;
import qa.ecom.base.BaseTest;
import qa.ecom.pages.CartPage;
import qa.ecom.pages.HomePage;
import qa.ecom.pages.ProductDetailsPage;
import qa.ecom.pages.WishlistPage;

import static org.assertj.core.api.Assertions.assertThat;

public class WishlistTest extends BaseTest {

    private String wishlistFirstProduct(HomePage home) {
        Product product = api.products().get(0);
        ProductDetailsPage details = home.openDetails(product.name());
        assertThat(details.addToWishlist()).isEqualTo("Added to Wishlist!");
        return product.name();
    }

    @Test(description = "A new user's wishlist is empty")
    public void wishlistStartsEmpty() {
        shopAsNewUser();

        assertThat(new WishlistPage(driver).open().isEmpty()).isTrue();
    }

    @Test(groups = "smoke", description = "A product added from its details page appears in the wishlist")
    public void addedProductAppearsInWishlist() {
        HomePage home = shopAsNewUser();
        String name = wishlistFirstProduct(home);

        home.nav().openWishlist();

        assertThat(new WishlistPage(driver).waitUntilLoaded().itemNames()).containsExactly(name);
    }

    @Test(description = "Removing the only wishlist item leaves the wishlist empty")
    public void removingTheOnlyItemEmptiesTheWishlist() {
        HomePage home = shopAsNewUser();
        String name = wishlistFirstProduct(home);
        home.nav().openWishlist();

        WishlistPage wishlist = new WishlistPage(driver).waitUntilLoaded().remove(name);

        wishlist.waitUntilItemRemoved(name);
        assertThat(wishlist.isEmpty()).isTrue();
    }

    @Test(description = "A wishlist item can be moved into the cart")
    public void wishlistItemCanBeAddedToCart() {
        HomePage home = shopAsNewUser();
        String name = wishlistFirstProduct(home);
        home.nav().openWishlist();

        String alert = new WishlistPage(driver).waitUntilLoaded().addToCart(name);
        home.nav().waitForCartCount(1).openCart();

        assertThat(alert).contains(name);
        assertThat(new CartPage(driver).waitUntilLoaded().itemNames()).containsExactly(name);
    }
}
