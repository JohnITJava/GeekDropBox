package com.geekbrains.common;

public class RegObject extends AbstractObject{
    private boolean isRegistered = false;

    public boolean isRegistered() {
        return isRegistered;
    }

    public RegObject(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

}
