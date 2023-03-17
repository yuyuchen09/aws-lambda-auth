package proxy;

import com.amazonaws.util.StringUtils;

/**
 * Evolve into an enum to hold all environment config params with defaults.
 */
public class EnvironmentWrapper {
    public static final String DEFAULT_REGION = "us-west-2";
    public static final String DEFAULT_TABLE_NAME = "csa-users";
    public static final String DEFAULT_USER_POOL_ID = "us-west-2_AjUDJ7k89";

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
        //...
        return get("Environment");
    }

    public static String getUserPoolId() {
        String userPoolId = get("USRE_POOL_ID");
        if (StringUtils.isNullOrEmpty(userPoolId)) {
            userPoolId = DEFAULT_USER_POOL_ID;
        }
        return userPoolId;
    }

    public static String getTableName() {
        String tableName = get("TABLE_NAME");
        if (StringUtils.isNullOrEmpty(tableName)) {
            tableName = DEFAULT_TABLE_NAME;
        }
        return tableName;
    }

    public static String getRegion() {
        String region = get("AWS_REGION");
        if (StringUtils.isNullOrEmpty(region)) {
            region = DEFAULT_REGION;
        }
        return region;
    }
}
