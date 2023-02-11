package com.workshop.mainserverworkshop.containers;

public class PlugInfoContainer {
    private String title, type, status, index;

    public PlugInfoContainer(String i_Title, String i_Type, String i_Status, String i_Index) {
            title = i_Title;
            type = i_Type;
            status = i_Status;
            index = i_Index;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getIndex() {
        return index;
    }
}
