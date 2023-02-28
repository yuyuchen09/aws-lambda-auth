package backend;

import java.util.Arrays;
import java.util.HashSet;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

/**
 *  Util for DynamoDB CRUD operations using the AWS SDK for Java document API.
 *  - table name 'csa-users'
 *  - support GET, POST and DELETE
 */
public class DynamoDBItemUtil {
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB = new DynamoDB(client);
    private static final String TABLE_NAME;

    static {
        String tableName = EnvironmentWrapper.getTableName();
        if (tableName == null || tableName.isBlank() || tableName.isEmpty()) {
            TABLE_NAME = "csa-users";
        } else {
            TABLE_NAME = tableName;
        }
    }

    public static void createItems() {
        Table table = dynamoDB.getTable(TABLE_NAME);
        try {
            Item item = new Item().withPrimaryKey("Id", 120).withString("Title", "Book 120 Title")
                    .withString("ISBN", "120-1111111111")
                    .withStringSet("Authors", new HashSet<String>(Arrays.asList("Author12", "Author22")))
                    .withNumber("Price", 20).withString("Dimensions", "8.5x11.0x.75").withNumber("PageCount", 500)
                    .withBoolean("InPublication", false).withString("ProductCategory", "Book");
            table.putItem(item);

            item = new Item().withPrimaryKey("Id", 121).withString("Title", "Book 121 Title")
                    .withString("ISBN", "121-1111111111")
                    .withStringSet("Authors", new HashSet<String>(Arrays.asList("Author21", "Author 22")))
                    .withNumber("Price", 20).withString("Dimensions", "8.5x11.0x.75").withNumber("PageCount", 500)
                    .withBoolean("InPublication", true).withString("ProductCategory", "Book");
            table.putItem(item);
        } catch (Exception e) {
            System.err.println("Create items failed.");
            System.err.println(e.getMessage());
        }
    }

    public static Item retrieveItem(String id) {
        Item item = null;
        Table table = dynamoDB.getTable(TABLE_NAME);
        try {
            item = table.getItem("Id", id, "id, name, email", null);
            System.out.println("Printing item after retrieving it...." + item.toJSONPretty());
            return item;
        } catch (Exception e) {
            System.err.println("GetItem failed." + e.getMessage());
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
            // Check the response.
            System.out.println("Printing item after adding new attribute...");
            System.out.println(outcome.getItem().toJSONPretty());
        } catch (Exception e) {
            System.err.println("Failed to add new attribute in " + TABLE_NAME);
            System.err.println(e.getMessage());
        }
    }

    public static void updateMultipleAttributes() {
        Table table = dynamoDB.getTable(TABLE_NAME);
        try {

            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Id", 120)
                    .withUpdateExpression("add #a :val1 set #na=:val2")
                    .withNameMap(new NameMap().with("#a", "Authors").with("#na", "NewAttribute"))
                    .withValueMap(
                            new ValueMap().withStringSet(":val1", "Author YY", "Author ZZ").withString(":val2", "someValue"))
                    .withReturnValues(ReturnValue.ALL_NEW);

            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            // Check the response.
            System.out.println("Printing item after multiple attribute update...");
            System.out.println(outcome.getItem().toJSONPretty());
        } catch (Exception e) {
            System.err.println("Failed to update multiple attributes in " + TABLE_NAME);
            System.err.println(e.getMessage());
        }
    }

    public static void updateExistingAttributeConditionally() {
        Table table = dynamoDB.getTable(TABLE_NAME);
        try {
            // Specify the desired price (25.00) and also the condition (price = 20.00)
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Id", 120)
                    .withReturnValues(ReturnValue.ALL_NEW).withUpdateExpression("set #p = :val1")
                    .withConditionExpression("#p = :val2").withNameMap(new NameMap().with("#p", "Price"))
                    .withValueMap(new ValueMap().withNumber(":val1", 25).withNumber(":val2", 20));

            UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
            // Check the response.
            System.out.println("Printing item after conditional update to new attribute...");
            System.out.println(outcome.getItem().toJSONPretty());
        } catch (Exception e) {
            System.err.println("Error updating item in " + TABLE_NAME);
            System.err.println(e.getMessage());
        }
    }

    public static DeleteItemOutcome deleteItem(Item item) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        String id = item.getString("id");
        String name = item.getString("name");
        try {
            DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey("id", id)
                    .withConditionExpression("#name = :val").withNameMap(new NameMap().with("#name", "name"))
                    .withValueMap(new ValueMap().withString(":val", name)).withReturnValues(ReturnValue.ALL_OLD);

            DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);
            System.out.println("Printing item that was deleted..." + outcome.getItem().toJSONPretty());
            return outcome;
        } catch (Exception e) {
            System.err.println("Error deleting item in " + TABLE_NAME + "." + e.getMessage());
        }

        return null;
    }
}
