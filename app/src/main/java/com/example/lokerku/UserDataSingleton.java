package com.example.lokerku;

public class UserDataSingleton {
    private static UserDataSingleton instance;
    private String name;
    private String username;
    private int[] arrRand = new int [2];

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

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int[] getArrRand() {
        return arrRand;
    }

    public void setArrRand(int[] arrRand) {
        this.arrRand = arrRand;
    }
}
