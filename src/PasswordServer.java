import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PasswordServer.java
 * Demonstrates:
 * 1. Controller / Transport Layer - Exposes RESTful HTTP API endpoints using standard Java SE HttpServer.
 * 2. Zero External Dependencies - Requires only standard JDK 17+ or 21+.
 * 3. Static File Server - Directly serves index.html, style.css, and script.js from the public/ folder.
 * 4. CORS Support - Allows frontend communication from any local port or origin.
 */
public class PasswordServer {

    private static final int DEFAULT_PORT = 8080;
    private static final String PUBLIC_DIR = "public";
    private final PasswordService passwordService;
    private final int port;

    public PasswordServer(int port) {
        this.port = port;
        this.passwordService = new PasswordService();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(this.port), 0);

        // 1. API Endpoints
        server.createContext("/api/check-password", new CheckPasswordHandler(this.passwordService));
        server.createContext("/api/generate-password", new GeneratePasswordHandler(this.passwordService));

        // 2. Static File Handler (serves public/ folder)
        server.createContext("/", new StaticFileHandler());

        // Default thread executor
        server.setExecutor(null);
        server.start();

        System.out.println("==================================================================");
        System.out.println("  SECUREPASS - Java OOP Password Checker & Generator Server       ");
        System.out.println("==================================================================");
        System.out.println("  Server is actively listening on: http://localhost:" + this.port);
        System.out.println("  Static assets folder: " + new File(PUBLIC_DIR).getAbsolutePath());
        System.out.println("  API Endpoints:");
        System.out.println("    - POST http://localhost:" + this.port + "/api/check-password");
        System.out.println("    - POST http://localhost:" + this.port + "/api/generate-password");
        System.out.println("  Press Ctrl+C to terminate the server.");
        System.out.println("==================================================================");
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number specified. Using default: " + DEFAULT_PORT);
            }
        }

        try {
            PasswordServer server = new PasswordServer(port);
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // HTTP Handlers
    // =========================================================================

    /**
     * Handles POST /api/check-password
     */
    static class CheckPasswordHandler implements HttpHandler {
        private final PasswordService service;

        public CheckPasswordHandler(PasswordService service) {
            this.service = service;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed. Use POST.");
                return;
            }

            try {
                String body = readRequestBody(exchange);
                String password = extractJsonString(body, "password");

                if (password == null) {
                    sendErrorResponse(exchange, 400, "Missing 'password' parameter in JSON payload.");
                    return;
                }

                PasswordResult result = this.service.checkPassword(password);
                sendJsonResponse(exchange, 200, result.toJson());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles POST /api/generate-password
     */
    static class GeneratePasswordHandler implements HttpHandler {
        private final PasswordService service;

        public GeneratePasswordHandler(PasswordService service) {
            this.service = service;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed. Use POST.");
                return;
            }

            try {
                String body = readRequestBody(exchange);

                int length = extractJsonInt(body, "length", 16);
                boolean useUpper = extractJsonBoolean(body, "includeUpper", true);
                boolean useLower = extractJsonBoolean(body, "includeLower", true);
                boolean useDigits = extractJsonBoolean(body, "includeDigits", true);
                boolean useSpecial = extractJsonBoolean(body, "includeSpecial", true);

                String jsonResponse = this.service.generatePasswordWithAnalysis(
                    length, useUpper, useLower, useDigits, useSpecial
                );

                sendJsonResponse(exchange, 200, jsonResponse);
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
    }

    /**
     * Static file handler for serving index.html, style.css, script.js
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod()) && !"HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendErrorResponse(exchange, 405, "Method Not Allowed.");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Security: Prevent directory traversal attacks
            if (path.contains("..")) {
                sendErrorResponse(exchange, 403, "Access Denied.");
                return;
            }

            File file = new File(PUBLIC_DIR, path);
            if (!file.exists() || file.isDirectory()) {
                // If not found, check if it's index.html or fallback
                File fallback = new File(PUBLIC_DIR, "index.html");
                if (fallback.exists()) {
                    file = fallback;
                } else {
                    sendErrorResponse(exchange, 404, "404 Not Found: " + path);
                    return;
                }
            }

            String mimeType = getMimeType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", mimeType + "; charset=utf-8");
            exchange.sendResponseHeaders(200, file.length());

            try (InputStream is = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
        }
    }

    // =========================================================================
    // Utilities & JSON Parsing
    // =========================================================================

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
            os.flush();
        }
    }

    private static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = "{\"error\":\"" + message.replace("\"", "\\\"") + "\"}";
        sendJsonResponse(exchange, statusCode, json);
    }

    private static String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".js")) return "application/javascript";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    // Lightweight robust JSON field extractors (Pure standard Java)
    public static String extractJsonString(String json, String key) {
        if (json == null) return null;
        // Standard JSON: "key" : "value"
        Pattern p1 = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"");
        Matcher m1 = p1.matcher(json);
        if (m1.find()) {
            return m1.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        // Tolerant matching: key : "value" or key : value
        Pattern p2 = Pattern.compile("(?i)(?:\"|')?" + Pattern.quote(key) + "(?:\"|')?\\s*:\\s*(?:\"|')?([^,\"}']+)(?:\"|')?");
        Matcher m2 = p2.matcher(json);
        if (m2.find()) {
            return m2.group(1).trim();
        }
        return null;
    }

    public static int extractJsonInt(String json, String key, int defaultValue) {
        if (json == null) return defaultValue;
        Pattern p = Pattern.compile("(?i)(?:\"|')?" + Pattern.quote(key) + "(?:\"|')?\\s*:\\s*(\\d+)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public static boolean extractJsonBoolean(String json, String key, boolean defaultValue) {
        if (json == null) return defaultValue;
        Pattern p = Pattern.compile("(?i)(?:\"|')?" + Pattern.quote(key) + "(?:\"|')?\\s*:\\s*(true|false)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return Boolean.parseBoolean(m.group(1));
        }
        return defaultValue;
    }
}
