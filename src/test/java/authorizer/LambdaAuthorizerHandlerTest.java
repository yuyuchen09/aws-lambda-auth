package authorizer;

import static api.AuthPolicy.ACTION;
import static api.AuthPolicy.EFFECT;
import static api.AuthPolicy.STATEMENT;
import static api.AuthPolicy.VERSION;

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
        assertEquals("2012-10-17", authPolicy.getPolicyDocument().get(VERSION));
        Map<String, Object>[] policies = (Map<String, Object>[]) authPolicy.getPolicyDocument().get(STATEMENT);
        assertTrue(policies[0].get(ACTION).equals("execute-api:Invoke"));
        assertTrue(policies[0].get(EFFECT).equals("Allow"));
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