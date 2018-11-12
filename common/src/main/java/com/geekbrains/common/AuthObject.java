package com.geekbrains.common;

import java.io.Serializable;

public class AuthObject extends AbstractObject{
    private String login;
    private boolean isAuthorized = false;

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public String getLogin() {
        return login;
    }

    public AuthObject(String login, boolean isAuthorized) {
        this.login = login;
        this.isAuthorized = isAuthorized;
    }

}
