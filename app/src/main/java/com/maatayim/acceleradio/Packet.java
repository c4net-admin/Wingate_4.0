package com.maatayim.acceleradio;

public class Packet {
    
    private String msg;
    private long timeMS;

    public Packet(String msg, long timeMS) {
        this.msg = msg;
        this.timeMS = timeMS;
    }

    public String getMsg() {
        return msg;
    }

    public long getTimeMS() {
        return timeMS;
    }
}
