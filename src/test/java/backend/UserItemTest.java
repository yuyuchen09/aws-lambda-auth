package backend;

import junit.framework.TestCase;

public class UserItemTest extends TestCase {

    public void testTestEquals() {
        // more test
    }

    public void testFromJson() {
        String testJson = "{\n" +
                "\t\"email\" : \"dev@gmail.com\",\n" +
                "\t\"fullName\" : \"dev\",\n" +
                "\t\"password\" : \"dev1235236\"\n" +
                "}";

        UserItem resultItem = UserItem.fromJson(testJson);
        UserItem jdoeItem = new UserItem("jdoe@gmail.com", "John Doe", "anything");
        UserItem devItem = new UserItem("dev@gmail.com", "dev", "anything");
        assertTrue(!jdoeItem.equals(resultItem));
        assertTrue(devItem.equals(resultItem));
    }

    public void testToJson() {
        //...
    }
}