package nd.esp.service.lifecycle.support.uc;



public class UserBaseInfo {
    
    private String userId;
    
    private String userName;
    
    private String nickName;
    
    public UserBaseInfo() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    
}