package com.example.lokerku;

public class Loker {

    String lokerAvailability;
    String lokerStatus;

    public String getLokerAvailability() {
        return lokerAvailability;
    }

    public String getLokerStatus() {
        return lokerStatus;
    }


    public void setLokerAvailability(String lokerAvailability) {
        this.lokerAvailability = lokerAvailability;
    }

    public void setLokerStatus(String lokerStatus) {
        this.lokerStatus = lokerStatus;
    }

    public void modelLoker(String lokerAvailability, String lokerStatus){
        this.lokerAvailability = lokerAvailability;
        this.lokerStatus = lokerStatus;
    }


}
