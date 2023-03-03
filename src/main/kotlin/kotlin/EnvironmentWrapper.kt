package kotlin

/**
 * Converted from java peer for Kotlin experiment.
 */
object EnvironmentWrapper {
    operator fun get(key: String?): String {
        val property = System.getProperty(key)
        return if (property == null || "" == property) {
            System.getenv(key)
        } else property
    }

    operator fun get(key: String?, vararg vars: Any?): String {
        return EnvironmentWrapper[String.format(key!!, *vars)]
    }

    val environment: String
        get() = EnvironmentWrapper["Environment"]
    val tableName: String
        // Table name: 'csa-users'
        get() = EnvironmentWrapper["TableName"]
    val region: String
        get() = EnvironmentWrapper["AWS_REGION"]
}