package backend;

import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

public class DynamoDBItemHandlerTest extends TestCase {

    @Test
    public void testGetEvent() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream eventStream = this.getClass().getResourceAsStream("apiGatewayProxyRequestEvent.json");
        APIGatewayProxyRequestEvent requestEvent = objectMapper.readValue(eventStream, APIGatewayProxyRequestEvent.class);
        APIGatewayProxyResponseEvent responseEvent;
        DynamoDBItemHandler handler = new DynamoDBItemHandler();
        responseEvent = handler.handleRequest(requestEvent, null);

        assertEquals(SC_OK, (int) responseEvent.getStatusCode());
    }
}