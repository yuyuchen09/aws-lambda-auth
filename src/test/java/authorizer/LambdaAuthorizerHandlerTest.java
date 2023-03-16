package authorizer;

import static api.AuthPolicy.IAMPolicyConstants.Action;
import static api.AuthPolicy.IAMPolicyConstants.Effect;
import static api.AuthPolicy.IAMPolicyConstants.Statement;
import static api.AuthPolicy.IAMPolicyConstants.Version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

import api.AuthPolicy;
import api.TokenAuthorizerContext;
import proxy.EnvironmentWrapper;

public class LambdaAuthorizerHandlerTest extends TestCase {

    public void testHandleRequestAllow() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream eventStream = getClass().getClassLoader().getResourceAsStream("request/authorizer/token_GET_dev.json");
        TokenAuthorizerContext input = objectMapper.readValue(eventStream, TokenAuthorizerContext.class);
        LambdaAuthorizerHandler handler = new LambdaAuthorizerHandler();
        AuthPolicy authPolicy = handler.handleRequest(input, null);

        AuthPolicy expectedAuthPolicy = new AuthPolicy("dev",
                AuthPolicy.PolicyDocument.getAllowAllPolicy(EnvironmentWrapper.DEFAULT_REGION, "#######", "xxxxx", "demo"));
        assertEquals("dev", authPolicy.getPrincipalId());
        assertEquals("2012-10-17", authPolicy.getPolicyDocument().get(Version.name()));
        Map<String, Object>[] policies = (Map<String, Object>[]) authPolicy.getPolicyDocument().get(Statement.name());
        assertTrue(policies[0].get(Action.name()).equals("execute-api:Invoke"));
        assertTrue(policies[0].get(Effect.name()).equals("Allow"));
    }

    public void testHandleRequestDeny() throws IOException {
        // ...
    }
    public void testHandleRequestAllowOne() throws IOException {
        // ...
    }
    public void testHandleRequestDenyOne() throws IOException {
        // ...
    }
}