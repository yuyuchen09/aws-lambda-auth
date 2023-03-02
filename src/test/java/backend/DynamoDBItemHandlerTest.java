package backend;

import static backend.UserItem.Key.password;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DynamoDBItemHandlerTest extends TestCase {

    @Override
    @BeforeAll
    public void setUp() throws Exception {
        List<UserItem> users = new ArrayList<>();
        users.add(new UserItem().withEmail("dev@gmail.com").withFullName("dev").withPassword(generatePassword("test123")));
        users.add(new UserItem().withEmail("jdoe@gmail.com").withFullName("John Doe").withPassword(generatePassword("test123")));
        users.add(new UserItem().withEmail("qa@gmail.com").withFullName("qa").withPassword(generatePassword("test123")));

        DynamoDBItemHandler handler = new DynamoDBItemHandler();
        for (UserItem user : users) {
            APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
            requestEvent.setHttpMethod(HttpMethod.POST.name());
            requestEvent.setBody(user.toJson());
            APIGatewayProxyResponseEvent deleteResponseEvent = handler.handleRequest(requestEvent, null);
        }
    }

    @Override
    @AfterAll
    public void tearDown() throws Exception {
        super.tearDown();
        // ...
    }

    @Test
    public void testGetEvent() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream eventStream = getClass().getClassLoader().getResourceAsStream("request/apiGatewayProxyRequestEvent_GET.json");
        APIGatewayProxyRequestEvent requestEvent = objectMapper.readValue(eventStream, APIGatewayProxyRequestEvent.class);
        DynamoDBItemHandler handler = new DynamoDBItemHandler();
        APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, null);
        // assertions
        assertEquals(SC_OK, (int) responseEvent.getStatusCode());
        UserItem resultItem = UserItem.fromJson(responseEvent.getBody());
        String expectedBody = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("expected/response_GET.json"));
        UserItem expectedItem = UserItem.fromJson(expectedBody);
        assertTrue(resultItem.equals(expectedItem));
    }

    @Test
    public void testGetEventNotFound() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setHttpMethod(HttpMethod.GET.name());
        Map<String, String> queryStringParameters = new HashMap<>();
        queryStringParameters.put("email", "dev120099@gmail.com");
        requestEvent.setQueryStringParameters(queryStringParameters);
        DynamoDBItemHandler handler = new DynamoDBItemHandler();
        APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, null);
        // assertions
        assertEquals(SC_NOT_FOUND, (int) responseEvent.getStatusCode());
    }

    @Test
    public void testPostEvent() throws IOException {
        DynamoDBItemHandler handler = new DynamoDBItemHandler();

        // make sure item doesn't exist
        APIGatewayProxyRequestEvent deleteRequestEvent = new APIGatewayProxyRequestEvent();
        deleteRequestEvent.setHttpMethod(HttpMethod.DELETE.name());
        Map<String, String> queryStringParameters = new HashMap<>();
        queryStringParameters.put("email", "dev@gmail.com");
        deleteRequestEvent.setQueryStringParameters(queryStringParameters);
        APIGatewayProxyResponseEvent deleteResponseEvent = handler.handleRequest(deleteRequestEvent, null);
        assertEquals(SC_NO_CONTENT, (int) deleteResponseEvent.getStatusCode());

        // create, 201
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream eventStream = getClass().getClassLoader().getResourceAsStream("request/apiGatewayProxyRequestEvent_POST.json");
        APIGatewayProxyRequestEvent requestEvent = objectMapper.readValue(eventStream, APIGatewayProxyRequestEvent.class);
        Item newItem = Item.fromJSON(requestEvent.getBody());
        newItem.withString(password.name(), generatePassword(newItem.getString(password.name())));
        requestEvent.setBody(newItem.toJSON());
        APIGatewayProxyResponseEvent responseEvent = handler.handleRequest(requestEvent, null);
        assertEquals(SC_CREATED, (int) responseEvent.getStatusCode());

        // create again, 409 || 201
        APIGatewayProxyResponseEvent responseEvent1 = handler.handleRequest(requestEvent, null);
        assertEquals(SC_CREATED, (int) responseEvent1.getStatusCode());
    }

    @Test
    public void testPostEventConflict() {
        // More tests for cases if item exists...
    }

    private String generatePassword(String password) {
        return password + randomInt(10000);
    }

    private int randomInt(int max) {
        return (int) Math.floor(Math.random() * (max - 1) + 1);
    }
}