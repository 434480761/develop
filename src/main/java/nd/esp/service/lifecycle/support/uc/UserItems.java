package nd.esp.service.lifecycle.support.uc;

public class UserItems extends Items<UserBaseInfo> {
    public boolean addAll(UserItems anotherUserItems) {
        for (UserBaseInfo userInfo : anotherUserItems) {
            getItems().add(userInfo);
        }
        return true;
    }
}