package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("credential")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class LoginDocument {
    private String login;
    private String substitute;
    private String password;

    public LoginDocument() { }

    public String getLogin() {
        return login;
    }

    public String getSubstitute() {
        return substitute;
    }

    public String getPassword() {
        return password;
    }

    public void setLogin(String login) {
        if (login.indexOf('#') != -1) {
            String[] parts = login.split("#");

            this.login = parts[0];
            this.substitute = parts[1];
        } else {
            this.login = login;
            this.substitute = null;
        }
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
