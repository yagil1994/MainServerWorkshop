package com.workshop.mainserverworkshop.engine.modes;

public class GenericMode extends java.util.EventObject{

    private final String message;

    public GenericMode(Object i_Source, String i_Message) {
        super(i_Source);
        this.message = i_Message;
    }

    public String getMessage() {
        return message;
    }

}
