package backend;

import com.google.gson.Gson;
import lombok.*;

@Data
@With
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserItem {
    protected static Gson GSON = new Gson();

    enum Key {
        email, fullName, password
    }

    private String email;
    private String fullName;
    private String password;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserItem)) {
            return false;
        }

        UserItem objItem = (UserItem) obj;
        return this.email.equals(objItem.email) && this.fullName.equals(objItem.fullName);
    }

    public static UserItem fromJson(String json) {
        return GSON.fromJson(json, UserItem.class);
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
