package com.workshop.mainserverworkshop.mediators;

import com.workshop.mainserverworkshop.engine.Plugs_Mediator;

public class UI_Mediator { //this mediator gets http requests from the ui(the main server behaves here as server)
    private static UI_Mediator instance = null;

    public static UI_Mediator getInstance() {
        if (instance == null) {
            instance = new UI_Mediator();
        }
        return instance;
    }

    public Plugs_Mediator getPlugs_mediator(){return Plugs_Mediator.getInstance();}
}
