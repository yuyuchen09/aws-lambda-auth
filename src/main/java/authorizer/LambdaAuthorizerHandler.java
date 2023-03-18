package authorizer;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import api.AuthPolicy;
import api.TokenAuthorizerContext;

/**
 * A Java custom authorizer for API Gateway.
 */
public class LambdaAuthorizerHandler implements RequestHandler<TokenAuthorizerContext, AuthPolicy> {
    private static final Logger LOGGER = Logger.getLogger(LambdaAuthorizerHandler.class.getName());

    @Override
    public AuthPolicy handleRequest(TokenAuthorizerContext input, Context context) {

        String authorizationToken = input.getAuthorizationToken();
        // Decode a JWT token in-line and produce the principal user identifier associated with the token
        // By RFC 7519: The “sub” (subject) claim identifies the principal that is the subject of the JWT.
        String principalId = "";
        String userName = "";
        try {
            principalId = JWTUtil.getData(authorizationToken, "sub");
            userName = JWTUtil.getData(authorizationToken, "username");
        } catch (Exception exception) {
            LOGGER.info(Level.INFO + "Unauthorized.");
            throw new RuntimeException("Unauthorized.");
        }

        // 401 Unauthorized: not recognized or invalid token
        if (principalId == null || principalId.isEmpty() || principalId.isBlank()) {
            LOGGER.info(Level.INFO + "Unauthorized user principal.");
            throw new RuntimeException("Unauthorized.");
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

        // if access is allowed, API Gateway will proceed with the endpoint proxy integration.

        // See specs at, https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-lambda-authorizer-output.html
        // this function must generate a policy that is associated with the recognized principal user ID.
        // depending on your use case, you might store policies in a DB, or generate them on the fly

        // keep in mind, the policy is cached for 5 minutes by default (TTL=300)
        // and will apply to subsequent calls to any method/resource in the RestApi made with the same token

        AuthPolicy authPolicy;
        switch (userName) {
            case "dev":
            case "ychen":
            case "admin":
                authPolicy = new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getAllowAllPolicy(region, awsAccountId, restApiId, stage));
                break;
            case "qa":
            default:
                authPolicy = new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId, restApiId, stage));
                break;
        }

        return authPolicy;
    }

}
