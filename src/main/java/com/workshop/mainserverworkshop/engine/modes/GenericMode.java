package com.workshop.mainserverworkshop.engine.modes;

public class GenericMode extends java.util.EventObject{

    private final String message;

    public GenericMode(Object source, String message) {
        super(source);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
