package com.qa.api.config;

/**
 * Settings come from -D system properties or environment variables, with
 * defaults for the public Restful Booker demo. Real credentials would be
 * supplied by CI secrets and never committed.
 */
public final class ApiConfig {

    private ApiConfig() {
    }

    public static String baseUri() {
        return value("api.base.uri", "API_BASE_URI", "https://restful-booker.herokuapp.com");
    }

    public static String username() {
        return value("api.username", "API_USERNAME", "admin");
    }

    public static String password() {
        return value("api.password", "API_PASSWORD", "password123");
    }

    /** Upper limit for a single response, in milliseconds. */
    public static long maxResponseTimeMs() {
        return Long.parseLong(value("api.max.response.ms", "API_MAX_RESPONSE_MS", "5000"));
    }

    private static String value(String property, String envVar, String fallback) {
        String fromProperty = System.getProperty(property);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String fromEnv = System.getenv(envVar);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }
        return fallback;
    }
}
