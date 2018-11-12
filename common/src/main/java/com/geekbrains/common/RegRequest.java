package com.geekbrains.common;


public class RegRequest extends AbstractObject {
    private String login;
    private String password;
    private boolean isRegistered;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public RegRequest(String login, String password) {
        this.login = login;
        this.password = password;
        this.isRegistered = false;
    }
}
