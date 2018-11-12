package com.geekbrains.common;


public class AuthRequest extends AbstractObject {
    private String login;
    private String password;
    private boolean isAuthorized;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
        this.isAuthorized = false;
    }
}
