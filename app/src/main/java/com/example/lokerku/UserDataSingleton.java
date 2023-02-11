package com.example.lokerku;

public class UserDataSingleton {
    private static UserDataSingleton instance;
    private String name;
    private boolean requested;

    private UserDataSingleton() {}

    public static UserDataSingleton getInstance() {
        if (instance == null) {
            instance = new UserDataSingleton();
        }
        return instance;
    }

    public String getName() {
        return name;
    }

    public boolean isRequested() {
        return requested;
    }

    public void setRequested(boolean requested) {
        this.requested = requested;
    }

    public void setName(String name) {
        this.name = name;
    }
}
