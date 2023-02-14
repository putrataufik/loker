package com.example.lokerku;

import java.sql.Timestamp;

public class ModelHistory {
    private String timestamp;
    private int noLoker;

    private String userID;

    public ModelHistory(String userID, String timestamp, int noLoker) {
        this.userID = userID;
        this.timestamp = timestamp;
        this.noLoker = noLoker;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getNoLoker() {
        return noLoker;
    }

    public void setNoLoker(int noLoker) {
        this.noLoker = noLoker;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
