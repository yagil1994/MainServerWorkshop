package com.workshop.mainserverworkshop.mediators;

public class UIMediator { //this mediator gets http requests from the ui(the main server behaves here as server)
    private static UIMediator instance = null;

    public static UIMediator getInstance() {
        if (instance == null) {
            instance = new UIMediator();
        }
        return instance;
    }

    public PlugsMediator getPlugsMediator(){return PlugsMediator.getStaticInstance();}
}
