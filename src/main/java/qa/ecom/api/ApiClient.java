package qa.ecom.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import qa.ecom.config.Config;
import qa.ecom.data.TestUser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thin client for the application's REST API. UI tests use it for fast setup (creating users, logging in)
 * and as ground truth to check what the UI displays. It is not used to assert the behaviour under test.
 */
public final class ApiClient {

    public static final String SESSION_COOKIE = "connect.sid";

    private static final ObjectMapper JSON = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    public record Product(int id, String name, double price, int categoryId) {
    }

    /** Registers the user and returns the value of the session cookie the server issued. */
    public String register(TestUser user) {
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri("/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(Map.of(
                        "email", user.email(), "password", user.password()))))
                .build());
        if (response.statusCode() != 201) {
            throw new IllegalStateException("Registering " + user.email() + " failed: "
                    + response.statusCode() + " " + response.body());
        }
        return response.headers().allValues("set-cookie").stream()
                .filter(c -> c.startsWith(SESSION_COOKIE + "="))
                .map(c -> c.substring(SESSION_COOKIE.length() + 1, c.indexOf(';')))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No session cookie in register response"));
    }

    public List<Product> products() {
        return products(Map.of());
    }

    public List<Product> products(Map<String, String> query) {
        StringBuilder path = new StringBuilder("/products");
        String separator = "?";
        for (Map.Entry<String, String> e : query.entrySet()) {
            path.append(separator).append(e.getKey()).append('=')
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
            separator = "&";
        }
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri(path.toString())).GET().build());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("GET " + path + " returned " + response.statusCode());
        }
        List<Product> products = new ArrayList<>();
        try {
            for (JsonNode node : JSON.readTree(response.body())) {
                products.add(new Product(
                        node.get("id").asInt(),
                        node.get("name").asText(),
                        node.get("price").asDouble(),
                        node.get("category_id").asInt()));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unreadable products response", e);
        }
        return products;
    }

    private static URI uri(String path) {
        return URI.create(Config.apiUrl() + path);
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException("API call failed: " + request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted calling " + request.uri(), e);
        }
    }

    private static String toJson(Object value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
