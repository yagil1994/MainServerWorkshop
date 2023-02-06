package com.workshop.mainserverworkshop.engine.modes;

public class SleepMode extends java.util.EventObject {
    private final String message;

    public SleepMode(Object source, String message) {
        super(source);


        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
