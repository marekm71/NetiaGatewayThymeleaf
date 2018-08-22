package pl.novaris.gateway.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class UserFormEmail {
    @NotNull
    @Email
    private String email;

    public UserFormEmail() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
