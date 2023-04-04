package com.workshop.mainserverworkshop.DB;

import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.mediators.PlugsMediator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.IOException;

@Document("PlugSaves")
public class PlugSave {

    @Id
    private String plugTitle;
    @Field
    private String plugType;
    @Field
    private int port;
    @Field
    private int internalPlugIndex;
    @Field
    private int UiIndex;
    @Field
    private int minElectricityVolt;
    @Field
    private int maxElectricityVolt;
    @Field
    private boolean status;
    @Field
    private boolean fakePlug;
    @Field
    private boolean overTimeFlag;
    @Field
    private boolean isInvalidPlug;

    public PlugSave(Plug plug) {
        this.plugTitle = plug.getPlugTitle();
        this.plugType = plug.getPlugType();
        this.port = plug.getPort();
        this.internalPlugIndex = plug.getInternalPlugIndex();
        this.UiIndex = plug.getUiIndex();
        this.minElectricityVolt = plug.getMinElectricityVolt();
        this.maxElectricityVolt = plug.getMaxElectricityVolt();
        this.status = plug.getStatus();

        this.fakePlug = plug.isFakePlug();
        this.overTimeFlag = plug.isOverTimeFlag();
        this.isInvalidPlug = plug.isInvalidPlug();
    }

    public PlugSave() {}

    public Plug toPlug(PlugsMediator plugsMediator) throws IOException {
        Process process = plugsMediator.CreateProcess(port);
        Plug plug = new Plug(process, port, plugTitle, plugType, plugsMediator, internalPlugIndex, UiIndex, minElectricityVolt, maxElectricityVolt);
        plug.UpdateFieldsFromDB(overTimeFlag, isInvalidPlug, status);

        return plug;
    }

    public String getPlugTitle() {
        return plugTitle;
    }

    public void setPlugTitle(String plugTitle) {
        this.plugTitle = plugTitle;
    }

    public String getPlugType() {
        return plugType;
    }

    public void setPlugType(String plugType) {
        this.plugType = plugType;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getInternalPlugIndex() {
        return internalPlugIndex;
    }

    public void setInternalPlugIndex(int internalPlugIndex) {
        this.internalPlugIndex = internalPlugIndex;
    }

    public int getUiIndex() {
        return UiIndex;
    }

    public void setUiIndex(int uiIndex) {
        UiIndex = uiIndex;
    }

    public int getMinElectricityVolt() {
        return minElectricityVolt;
    }

    public void setMinElectricityVolt(int minElectricityVolt) {
        this.minElectricityVolt = minElectricityVolt;
    }

    public int getMaxElectricityVolt() {
        return maxElectricityVolt;
    }

    public void setMaxElectricityVolt(int maxElectricityVolt) {
        this.maxElectricityVolt = maxElectricityVolt;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isFakePlug() {
        return fakePlug;
    }

    public void setFakePlug(boolean fakePlug) {
        this.fakePlug = fakePlug;
    }

    public boolean isOverTimeFlag() {
        return overTimeFlag;
    }

    public void setOverTimeFlag(boolean overTimeFlag) {
        this.overTimeFlag = overTimeFlag;
    }

    public boolean isInvalidPlug() {
        return isInvalidPlug;
    }

    public void setInvalidPlug(boolean invalidPlug) {
        isInvalidPlug = invalidPlug;
    }
}

