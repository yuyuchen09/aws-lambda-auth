package proxy;

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

    // Table name: 'csa-users'
    public static String getTableName() {
        return get("TableName");
    }

    public static String getRegion() {
        return get("AWS_REGION");
    }
}
