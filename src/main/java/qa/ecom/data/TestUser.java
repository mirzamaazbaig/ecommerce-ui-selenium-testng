package qa.ecom.data;

import java.util.UUID;

/** A customer account used by a test. */
public record TestUser(String email, String password) {

    public static final String DEFAULT_PASSWORD = "TestPass123!";

    /** A new, unique user so tests never depend on each other's data. */
    public static TestUser unique() {
        return new TestUser("sel_" + UUID.randomUUID().toString().substring(0, 12) + "@example.com", DEFAULT_PASSWORD);
    }
}
