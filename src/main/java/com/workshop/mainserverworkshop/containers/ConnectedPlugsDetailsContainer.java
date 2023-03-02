package com.workshop.mainserverworkshop.containers;

public class ConnectedPlugsDetailsContainer {
    private String title, index, status,type;
    //private String internalIndex;

    public ConnectedPlugsDetailsContainer(String i_Title, String i_Index, String i_Status/*,String i_InternalIndex*/, String i_Type) {
        title = i_Title;
        index = i_Index;
        status = i_Status;
        //internalIndex = i_InternalIndex;
        type = i_Type;
    }

    public String getTitle() {
        return title;
    }

    public String getIndex() {
        return index;
    }

    public String getStatus() {
        return status;
    }

    //public String getInternalIndex() {return internalIndex;}

    public String getType() {
        return type;
    }
}
