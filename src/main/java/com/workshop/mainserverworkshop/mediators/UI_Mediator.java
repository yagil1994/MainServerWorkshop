package com.workshop.mainserverworkshop.mediators;
import com.google.gson.Gson;

public class UI_Mediator { //this mediator gets http requests from the ui(the main server behaves here as server)
    private Plugs_Mediator plugs_mediator;
    private static UI_Mediator instance = null;
    private Gson gson;

    public static UI_Mediator getInstance() {
        if (instance == null) {
            instance = new UI_Mediator();
        }
        return instance;
    }

    private UI_Mediator()
    {
        plugs_mediator = Plugs_Mediator.getInstance();
        gson = new Gson();
    }

    public Plugs_Mediator getPlugs_mediator(){return Plugs_Mediator.getInstance();}
}
