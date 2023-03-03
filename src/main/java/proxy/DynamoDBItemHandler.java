package proxy;

import static proxy.UserItem.Key.email;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

import java.text.MessageFormat;
import java.util.logging.Logger;

import org.apache.http.HttpException;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Handler for an endpoint Lambda microservice for DynamoDB CRUD operations.
 * - HEAD, OPTION, PATCH, PUT, or batch operation unsupported
 */
public class DynamoDBItemHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER = Logger.getLogger(DynamoDBItemHandler.class.getName());

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();

        LOGGER.info("apiGatewayProxyRequestEvent: " + apiGatewayProxyRequestEvent);
        HttpMethod httpMethod = HttpMethod.valueOf(apiGatewayProxyRequestEvent.getHttpMethod());
        try {
            switch (httpMethod) {
                case DELETE:
                    String emailToDelete = apiGatewayProxyRequestEvent.getQueryStringParameters().get(email.name().toLowerCase());
                    DeleteItemOutcome outcome = DynamoDBItemUtil.deleteItem(emailToDelete);
                    responseEvent.withStatusCode(SC_NO_CONTENT);
                    if (outcome != null) {
                        responseEvent.withBody(outcome.getItem().toJSONPretty());
                    }
                    break;
                case GET:
                    String email = apiGatewayProxyRequestEvent.getQueryStringParameters().get(UserItem.Key.email.name());
                    Item itemRetrieved = DynamoDBItemUtil.retrieveItem(email);

                    if (itemRetrieved != null) {
                        responseEvent.withStatusCode(SC_OK)
                                .withBody(itemRetrieved.toJSONPretty());
                    } else {
                        responseEvent.withStatusCode(SC_NOT_FOUND);
                    }
                    break;
                case POST:
                    Item item = Item.fromJSON(apiGatewayProxyRequestEvent.getBody());
                    PutItemOutcome putItemOutcome = DynamoDBItemUtil.createItem(item);
                    if (putItemOutcome != null) {
                        responseEvent.withStatusCode(SC_CREATED);
                    } else {
                        responseEvent.withStatusCode(SC_CONFLICT);
                    }
                    break;
                default: // HEAD, OPTION, PATCH, PUT unsupported
                    throw new HttpException("Unsupported Method: {0}");
            }
        } catch (Exception e) {
            String errMsg = MessageFormat.format("Error handling API gateway proxy request: {0}", e.getMessage());
            LOGGER.info("DynamoDBItemCRUDHandler: " + errMsg);
            responseEvent.setStatusCode(SC_INTERNAL_SERVER_ERROR);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(SC_INTERNAL_SERVER_ERROR)
                    .withBody(errMsg);
        }

        return responseEvent;
    }
}


