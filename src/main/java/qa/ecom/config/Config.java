package qa.ecom.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

/**
 * Test configuration. Lookup order for every key: environment variable (BASE_URL), system property (-DbaseUrl),
 * then the defaults in config.properties.
 */
public final class Config {

    private static final Properties DEFAULTS = load();

    private Config() {
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = Config.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read config.properties", e);
        }
        return props;
    }

    private static String get(String key) {
        String envName = key.replaceAll("([A-Z])", "_$1").toUpperCase();
        String fromEnv = System.getenv(envName);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        String fromProperty = System.getProperty(key);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        return DEFAULTS.getProperty(key);
    }

    private static Optional<String> optional(String key) {
        return Optional.ofNullable(get(key)).filter(v -> !v.isBlank());
    }

    public static String baseUrl() {
        return stripTrailingSlash(get("baseUrl"));
    }

    public static String apiUrl() {
        return stripTrailingSlash(get("apiUrl"));
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless"));
    }

    public static Duration timeout() {
        return Duration.ofSeconds(Long.parseLong(get("timeoutSeconds")));
    }

    /** Optional path to a Chrome/Chromium binary (default: the one Selenium finds). */
    public static Optional<String> chromeBinary() {
        return optional("chromeBinary");
    }

    /** Optional path to a chromedriver (default: Selenium Manager downloads a matching one). */
    public static Optional<String> chromedriverPath() {
        return optional("chromedriverPath");
    }

    /** Needed when Chrome runs as root, for example in some containers. */
    public static boolean noSandbox() {
        return Boolean.parseBoolean(optional("noSandbox").orElse("false"));
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
