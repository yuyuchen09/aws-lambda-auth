package proxy;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

/**
 * Util for DynamoDB CRUD operations using the AWS SDK for Java document API.
 * - table name 'csa-users'
 * - support GET, POST and DELETE
 */
public class DynamoDBItemUtil {
    private static final Logger LOGGER = Logger.getLogger(DynamoDBItemUtil.class.getName());

    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    private static final String TABLE_NAME;


    static { // load config params from environment
        String tableName = EnvironmentWrapper.getTableName();
        if (tableName == null || tableName.isBlank() || tableName.isEmpty()) {
            TABLE_NAME = "csa-users";
        } else {
            TABLE_NAME = tableName;
        }
    }

    /**
     * Security password,
     * - In transit, Lambda API is only supported on HTTPS according to AWS Regions and Endpoints documentation.
     * For Lambda Proxy integration, all requests are proxied "as is" to the endpoint Lambda.
     * - At rest, All user data stored in Amazon DynamoDB is fully encrypted at rest by default using AWS KMS.
     * See in test event, header "X-Forwarded-Port": "443"
     * - Spring security for password encryption, <a href= https://docs.spring.io/spring-security/site/docs/5.0.0.RELEASE/api/>BCryptPasswordEncoder</a>
     *
     * @param item to create.
     */
    public static PutItemOutcome createItem(Item item) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        try {
            String password = item.getString(UserItem.Key.password.name());
            item.withString(UserItem.Key.password.name(), UserItem.generateSecurePassword(password));  // BCrypt encrypted pswd
            PutItemOutcome putItemOutcome = table.putItem(item);
            if (putItemOutcome != null && putItemOutcome.getItem() != null) {
                LOGGER.log(Level.INFO, "Item created: " + putItemOutcome.getItem().toJSONPretty());
            }
            return putItemOutcome;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "CreateItem failed." + e.getMessage());
        }

        return null;
    }

    public static Item retrieveItem(String email) {
        Item item;
        Table table = dynamoDB.getTable(TABLE_NAME);
        try {
            // Partition/Hash key: "email"; No global secondary index createdmvn
            item = table.getItem(UserItem.Key.email.name(), email, "email, fullName", null);
            LOGGER.info("Item after retrieving it...." + item.toJSONPretty());
            return item;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieveItem: " + email + "." + e.getMessage());
        }

        return null;
    }

    public static void updateAddNewAttribute() {
        Table table = dynamoDB.getTable(TABLE_NAME);

        try {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Id", 121)
                    .withUpdateExpression("set #na = :val1").withNameMap(new NameMap().with("#na", "NewAttribute"))
                    .withValueMap(new ValueMap().withString(":val1", "Some value")).withReturnValues(ReturnValue.ALL_NEW);

            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            LOGGER.log(Level.INFO, "Item after adding new attribute: " + outcome.getItem().toJSONPretty());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "updateAddNewAttribute failed to add new attribute in " + TABLE_NAME + "." + e.getMessage());
        }
    }

    public static void updateExistingAttributeConditionally() {
        Table table = dynamoDB.getTable(TABLE_NAME);
        try {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Id", 120)
                    .withReturnValues(ReturnValue.ALL_NEW).withUpdateExpression("set #p = :val1")
                    .withConditionExpression("#p = :val2").withNameMap(new NameMap().with("#p", "Price"))
                    .withValueMap(new ValueMap().withNumber(":val1", 25).withNumber(":val2", 20));

            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            LOGGER.log(Level.INFO, "Item after conditional update to new attribute: " + outcome.getItem().toJSONPretty());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating item in " + TABLE_NAME + "." + e.getMessage());
        }
    }

    public static DeleteItemOutcome deleteItem(String email) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        try {
            DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(UserItem.Key.email.name(), email);
            DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);
            LOGGER.log(Level.INFO, "Item was deleted: " + outcome.getItem().toJSONPretty());
            return outcome;
        } catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.log(Level.INFO, "Error deleting item, possibly item does not exist: " + TABLE_NAME + "." + illegalArgumentException.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error deleting item in " + TABLE_NAME + "." + e.getMessage());
        }

        return null;
    }
}
