package api;

import static api.AuthPolicy.IAMPolicyConstants.Action;
import static api.AuthPolicy.IAMPolicyConstants.Condition;
import static api.AuthPolicy.IAMPolicyConstants.Effect;
import static api.AuthPolicy.IAMPolicyConstants.Resource;
import static api.AuthPolicy.IAMPolicyConstants.Version;
import static api.AuthPolicy.IAMPolicyConstants.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

/**
 * AuthPolicy receives a set of allowed and denied methods and generates a valid
 * AWS policy for the API Gateway authorizer. The constructor receives the calling
 * user principal, the AWS account ID of the API owner, and an apiOptions object.
 * The apiOptions can contain an API Gateway RestApi Id, a region for the RestApi, and a
 * stage that calls should be allowed/denied for. For example
 *
 * new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId, restApiId, stage));
 *
 * TODO just use Package com.amazonaws.auth.policy
 * The following code creates a policy to allow a specific AWS account to send and receive messages using one of your Amazon SQS queues:
 *
 *     Policy policy = new Policy("MyQueuePolicy");
 *     policy.withStatements(new Statement(Effect.Allow)
 *            .withPrincipals(new Principal("123456789012"))
 *            .withActions(SQSActions.SendMessage, SQSActions.ReceiveMessage)
 *            .withResources(new SQSQueueResource("987654321000", "queue2")));
 */
public class AuthPolicy {

    // IAM Policy Constants
    public enum IAMPolicyConstants {
        Version, Statement, Effect, Action, NotAction, Resource, NotResource, Condition
    }

    String principalId;
    transient AuthPolicy.PolicyDocument policyDocumentObject;
    Map<String, Object> policyDocument;

    public AuthPolicy(String principalId, AuthPolicy.PolicyDocument policyDocumentObject) {
        this.principalId = principalId;
        this.policyDocumentObject = policyDocumentObject;
    }

    public AuthPolicy() {
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    /**
     * IAM Policies use capitalized field names, but Lambda by default will serialize object members using camel case
     *
     * This method implements a custom serializer to return the IAM Policy as a well-formed JSON document, with the correct field names
     *
     * @return IAM Policy as a well-formed JSON document
     */
    public Map<String, Object> getPolicyDocument() {
        Map<String, Object> serializablePolicy = new HashMap<>();
        serializablePolicy.put(Version.name(), policyDocumentObject.Version);
        Statement[] statements = policyDocumentObject.getStatement();
        Map<String, Object>[] serializableStatementArray = new Map[statements.length];
        for (int i = 0; i < statements.length; i++) {
            Map<String, Object> serializableStatement = new HashMap<>();
            AuthPolicy.Statement statement = statements[i];
            serializableStatement.put(Effect.name(), statement.Effect);
            serializableStatement.put(Action.name(), statement.Action);
            serializableStatement.put(Resource.name(), statement.getResource());
            serializableStatement.put(Condition.name(), statement.getCondition());
            serializableStatementArray[i] = serializableStatement;
        }
        serializablePolicy.put(Statement.name(), serializableStatementArray);
        return serializablePolicy;
    }

    public void setPolicyDocument(PolicyDocument policyDocumentObject) {
        this.policyDocumentObject = policyDocumentObject;
    }

    /**
     * PolicyDocument represents an IAM Policy, specifically for the execute-api:Invoke action
     * in the context of an API Gateway Authorizer
     *
     * Initialize the PolicyDocument with
     *   the region where the RestApi is configured,
     *   the AWS Account ID that owns the RestApi,
     *   the RestApi identifier
     *   and the Stage on the RestApi that the Policy will apply to
     */
    public static class PolicyDocument {

        static final String EXECUTE_API_ARN_FORMAT = "arn:aws:execute-api:%s:%s:%s/%s/%s/%s";

        String Version = "2012-10-17"; // override if necessary

        private final Statement allowStatement;
        private final Statement denyStatement;
        private final List<Statement> statements;

        // context metadata
        transient String region;
        transient String awsAccountId;
        transient String restApiId;
        transient String stage;

        /**
         * Creates a new PolicyDocument with the given context,
         * and initializes two base Statement objects for allowing and denying access to API Gateway methods
         *
         * @param region the region where the RestApi is configured
         * @param awsAccountId the AWS Account ID that owns the RestApi
         * @param restApiId the RestApi identifier
         * @param stage and the Stage on the RestApi that the Policy will apply to
         */
        public PolicyDocument(String region, String awsAccountId, String restApiId, String stage) {
            this.region = region;
            this.awsAccountId = awsAccountId;
            this.restApiId = restApiId;
            this.stage = stage;
            allowStatement = AuthPolicy.Statement.getEmptyInvokeStatement("Allow");
            denyStatement = AuthPolicy.Statement.getEmptyInvokeStatement("Deny");
            this.statements = new ArrayList<>();
            statements.add(allowStatement);
            statements.add(denyStatement);
        }

        public String getVersion() {
            return Version;
        }

        public void setVersion(String version) {
            Version = version;
        }

        public AuthPolicy.Statement[] getStatement() {
            return statements.toArray(new AuthPolicy.Statement[0]);
        }

        public void allowMethod(HttpMethod httpMethod, String resourcePath) {
            addResourceToStatement(allowStatement, httpMethod, resourcePath);
        }

        public void denyMethod(HttpMethod httpMethod, String resourcePath) {
            addResourceToStatement(denyStatement, httpMethod, resourcePath);
        }

        public void addStatement(AuthPolicy.Statement statement) {
            statements.add(statement);
        }

        private void addResourceToStatement(Statement statement, HttpMethod httpMethod, String resourcePath) {
            // resourcePath must start with '/'
            // to specify the root resource only, resourcePath should be an empty string
            if (resourcePath.equals("/")) {
                resourcePath = "";
            }
            String resource = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            String method = httpMethod == HttpMethod.ALL ? "*" : httpMethod.toString();
            statement.addResource(String.format(EXECUTE_API_ARN_FORMAT, region, awsAccountId, restApiId, stage, method, resource));
        }

        // Static methods

        /**
         * Generates a new PolicyDocument with a single statement that allows the requested method/resourcePath
         *
         * @param region API Gateway region
         * @param awsAccountId AWS Account that owns the API Gateway RestApi
         * @param restApiId RestApi identifier
         * @param stage Stage name
         * @param method HttpMethod to allow
         * @param resourcePath Resource path to allow
         * @return new PolicyDocument that allows the requested method/resourcePath
         */
        public static PolicyDocument getAllowOnePolicy(String region, String awsAccountId, String restApiId, String stage, HttpMethod method, String resourcePath) {
            AuthPolicy.PolicyDocument policyDocument = new AuthPolicy.PolicyDocument(region, awsAccountId, restApiId, stage);
            policyDocument.allowMethod(method, resourcePath);
            return policyDocument;

        }


        /**
         * Generates a new PolicyDocument with a single statement that denies the requested method/resourcePath
         *
         * @param region API Gateway region
         * @param awsAccountId AWS Account that owns the API Gateway RestApi
         * @param restApiId RestApi identifier
         * @param stage Stage name
         * @param method HttpMethod to deny
         * @param resourcePath Resource path to deny
         * @return new PolicyDocument that denies the requested method/resourcePath
         */
        public static PolicyDocument getDenyOnePolicy(String region, String awsAccountId, String restApiId, String stage, HttpMethod method, String resourcePath) {
            AuthPolicy.PolicyDocument policyDocument = new AuthPolicy.PolicyDocument(region, awsAccountId, restApiId, stage);
            policyDocument.denyMethod(method, resourcePath);
            return policyDocument;

        }

        public static AuthPolicy.PolicyDocument getAllowAllPolicy(String region, String awsAccountId, String restApiId, String stage) {
            return getAllowOnePolicy(region, awsAccountId, restApiId, stage, HttpMethod.ALL, "*");
        }

        public static PolicyDocument getDenyAllPolicy(String region, String awsAccountId, String restApiId, String stage) {
            return getDenyOnePolicy(region, awsAccountId, restApiId, stage, HttpMethod.ALL, "*");
        }
    }

    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, ALL
    }

    static class Statement {

        String Effect;
        String Action;
        Map<String, Map<String, Object>> Condition;

        private List<String> resourceList;

        public Statement() {

        }

        public Statement(String effect, String action, List<String> resourceList, Map<String, Map<String, Object>> condition) {
            this.Effect = effect;
            this.Action = action;
            this.resourceList = resourceList;
            this.Condition = condition;
        }

        public static Statement getEmptyInvokeStatement(String effect) {
            return new Statement(effect, "execute-api:Invoke", new ArrayList<String>(), new HashMap<String, Map<String, Object>>());
        }

        public String getEffect() {
            return Effect;
        }

        public void setEffect(String effect) {
            this.Effect = effect;
        }

        public String getAction() {
            return Action;
        }

        public void setAction(String action) {
            this.Action = action;
        }

        public String[] getResource() {
            return resourceList.toArray(new String[0]);
        }

        public void addResource(String resource) {
            resourceList.add(resource);
        }

        public Map<String, Map<String,Object>> getCondition() {
            return Condition;
        }

        public void addCondition(String operator, String key, Object value) {
            Condition.put(operator, Collections.singletonMap(key, value));
        }

    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
