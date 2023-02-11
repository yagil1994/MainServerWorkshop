package com.workshop.mainserverworkshop.containers;

public class ConnectedPlugsDetailsContainer {

    private String title,index;

    public ConnectedPlugsDetailsContainer(String i_Title, String i_Index)
    {
        title = i_Title;
        index = i_Index;
    }

    public String getTitle() {
        return title;
    }

    public String getIndex() {
        return index;
    }
}
