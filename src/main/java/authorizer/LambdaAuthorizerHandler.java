package authorizer;

import io.AuthPolicy;
import io.TokenAuthorizerContext;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Handles IO for a Java Lambda function as a custom authorizer for API Gateway.
 *
 * - JWT secret key support
 */
public class LambdaAuthorizerHandler implements RequestHandler<TokenAuthorizerContext, AuthPolicy> {

    @Override
    public AuthPolicy handleRequest(TokenAuthorizerContext input, Context context) {

        String authorizationToken = input.getAuthorizationToken();
        // Decode a JWT token in-line and produce the principal user identifier associated with the token
        // By RFC 7519: The “sub” (subject) claim identifies the principal that is the subject of the JWT.
        String principalId = "ychen";
        try {
            principalId = JWTUtil.getSub(authorizationToken);
        } catch (Exception exception) {
            // logging anything here
        }

        // 401 Unauthorized: not recognized or invalid token
        if (principalId == null || principalId.isEmpty() || principalId.isBlank()) {
            throw new RuntimeException("Unauthorized");
        }

        // if the token is valid, a policy should be generated which will allow or deny access to the client
        String methodArn = input.getMethodArn();
        String[] arnPartials = methodArn.split(":");
        String region = arnPartials[3];
        String awsAccountId = arnPartials[4];
        String[] apiGatewayArnPartials = arnPartials[5].split("/");
        String restApiId = apiGatewayArnPartials[0];
        String stage = apiGatewayArnPartials[1];
        String httpMethod = apiGatewayArnPartials[2];
        String resource = ""; // root resource
        if (apiGatewayArnPartials.length == 4) {
            resource = apiGatewayArnPartials[3];
        }

        // 403 Access Denied: if access is denied
        // TODO

        // if access is allowed, API Gateway will proceed with the back-end integration configured on the method that was called

        // this function must generate a policy that is associated with the recognized principal user identifier.
        // depending on your use case, you might store policies in a DB, or generate them on the fly

        // keep in mind, the policy is cached for 5 minutes by default (TTL is configurable in the authorizer)
        // and will apply to subsequent calls to any method/resource in the RestApi
        // made with the same token

        // the example policy below denies access to all resources in the RestApi
        return new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getAllowAllPolicy(region, awsAccountId, restApiId, stage));

//        return new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId, restApiId, stage));
    }

}
