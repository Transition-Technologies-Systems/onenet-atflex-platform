package pl.com.tt.flex.server.web.rest.vm.user;

/**
 * View Model object for storing the user's key and password.
 */
public class KeyAndPasswordVM {

    private String key;

    private String newPassword;

    private String newLogin;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewLogin() { return newLogin; }

    public void setNewLogin(String newLogin) { this.newLogin = newLogin; }
}
