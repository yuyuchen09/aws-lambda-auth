package backend;

import java.time.Duration;

public class EnvironmentWrapper {

    public static String get(String key) {
        String property = System.getProperty(key);
        if (property == null || "".equals(property)) {
            return System.getenv(key);
        }
        return property;
    }

    public static String get(String key, Object... vars) {
        return get(String.format(key, vars));
    }

    public static String getEnvironment() {
        return get("Environment");
    }

    public static String getRegion() {
        return get("AWS_REGION");
    }

    public static Duration getApacheConnectionTimeout() {
        return getAsDuration("apacheConnectionTimeout", 60l);
    }

    public static Duration getApacheSocketTimeout() {
        return getAsDuration("apacheSocketTimeout", 60l);
    }

    public static Duration getApacheConnectionAcquisitionTimeout() {
        return getAsDuration("apacheConnectionAcquisitionTimeout", 60l);
    }

    public static int getMaxRetries() {
        return Integer.parseInt(get("maxRetries"));
    }

    public static int getRetryDelay() {
        return Integer.parseInt(get("retryDelay"));
    }

    private static Duration getAsDuration(String key, long defaultValue) {
        try {
            return Duration.ofSeconds(Long.parseLong(get(key)));
        } catch (NumberFormatException exception) {
            return Duration.ofSeconds(defaultValue);
        }
    }
}
