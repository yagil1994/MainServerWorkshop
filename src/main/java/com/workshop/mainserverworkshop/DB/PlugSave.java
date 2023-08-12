package com.workshop.mainserverworkshop.DB;

import com.workshop.mainserverworkshop.containers.AllStatisticsContainer;
import com.workshop.mainserverworkshop.engine.Plug;
import com.workshop.mainserverworkshop.mediators.PlugsMediator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.IOException;
import java.util.LinkedList;

@Document("PlugSavesLocal")
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
    private float minElectricityVolt;
    @Field
    private float maxElectricityVolt;
    @Field
    private boolean status;
    @Field
    private boolean fakePlug;
    @Field
    private boolean overTimeFlag;
    @Field
    private boolean isInvalidPlug;
    @Field
    private boolean registeredToSleepMode;
    @Field
    private boolean registeredToSafeMode;
    @Field
    private float electricityUsageTillNow;
    @Field
    private float lastSingleUsageStatistics;
    @Field
    private float[] lastWeeklyStatistics;
    @Field
    private float[] lastAnnualStatistics;
    @Field
    private LinkedList<Float> learningUsages;
    @Field
    private boolean finishedElectricityUsageLearning;
    @Field
    public int learningTimes;
    @Field
    private float invalidUsageVolt;
    @Field
    private float avgElectricityUsageAfterLearning;

    public PlugSave(Plug plug, boolean i_RegisteredToSleepMode, boolean i_RegisteredToSafeMode) {
        plugTitle = plug.getPlugTitle();
        plugType = plug.getPlugType();
        port = plug.getPort();
        internalPlugIndex = plug.getInternalPlugIndex();
        UiIndex = plug.getUiIndex();
        minElectricityVolt = plug.getMinElectricityVolt();
        maxElectricityVolt = plug.getMaxElectricityVolt();
        status = plug.getStatus();
        fakePlug = plug.isFakePlug();
        overTimeFlag = plug.isOverTimeFlag();
        isInvalidPlug = plug.isInvalidPlug();
        registeredToSleepMode = i_RegisteredToSleepMode;
        registeredToSafeMode = i_RegisteredToSafeMode;
        AllStatisticsContainer allStatisticsContainer = plug.getAllStatisticsContainer();
        electricityUsageTillNow = allStatisticsContainer.getElectricityUsageTillNow();
        lastSingleUsageStatistics = allStatisticsContainer.getLastSingleUsageStatistics();
        lastWeeklyStatistics = allStatisticsContainer.getLastWeeklyStatistics();
        lastAnnualStatistics = allStatisticsContainer.getLastAnnualStatistics();
        invalidUsageVolt = plug.getInvalidUsageVolt();
        avgElectricityUsageAfterLearning = plug.getAvgElectricityUsageAfterLearning();
        learningUsages = plug.getLearningUsages();
        learningTimes = plug.getLearningTimes();
        finishedElectricityUsageLearning = plug.isFinishedElectricityUsageLearning();
    }

    public PlugSave() {
    }

    public void setMinElectricityVolt(float minElectricityVolt) {
        this.minElectricityVolt = minElectricityVolt;
    }

    public void setMaxElectricityVolt(float maxElectricityVolt) {
        this.maxElectricityVolt = maxElectricityVolt;
    }

    public boolean isStatus() {
        return status;
    }

    public float getElectricityUsageTillNow() {
        return electricityUsageTillNow;
    }

    public void setElectricityUsageTillNow(float electricityUsageTillNow) {
        this.electricityUsageTillNow = electricityUsageTillNow;
    }

    public float getLastSingleUsageStatistics() {
        return lastSingleUsageStatistics;
    }

    public void setLastSingleUsageStatistics(float lastSingleUsageStatistics) {
        this.lastSingleUsageStatistics = lastSingleUsageStatistics;
    }

    public float[] getLastWeeklyStatistics() {
        return lastWeeklyStatistics;
    }

    public void setLastWeeklyStatistics(float[] lastWeeklyStatistics) {
        this.lastWeeklyStatistics = lastWeeklyStatistics;
    }

    public float[] getLastAnnualStatistics() {
        return lastAnnualStatistics;
    }

    public void setLastAnnualStatistics(float[] lastAnnualStatistics) {
        this.lastAnnualStatistics = lastAnnualStatistics;
    }

    public LinkedList<Float> getLearningUsages() {
        return learningUsages;
    }

    public void setLearningUsages(LinkedList<Float> learningUsages) {
        this.learningUsages = learningUsages;
    }

    public boolean isFinishedElectricityUsageLearning() {
        return finishedElectricityUsageLearning;
    }

    public void setFinishedElectricityUsageLearning(boolean finishedElectricityUsageLearning) {
        this.finishedElectricityUsageLearning = finishedElectricityUsageLearning;
    }

    public int getLearningTimes() {
        return learningTimes;
    }

    public void setLearningTimes(int learningTimes) {
        this.learningTimes = learningTimes;
    }

    public float getInvalidUsageVolt() {
        return invalidUsageVolt;
    }

    public void setInvalidUsageVolt(float invalidUsageVolt) {
        this.invalidUsageVolt = invalidUsageVolt;
    }

    public float getAvgElectricityUsageAfterLearning() {
        return avgElectricityUsageAfterLearning;
    }

    public void setAvgElectricityUsageAfterLearning(float avgElectricityUsageAfterLearning) {
        this.avgElectricityUsageAfterLearning = avgElectricityUsageAfterLearning;
    }

    public Plug toPlug(PlugsMediator plugsMediator) throws IOException {
        Process process = plugsMediator.CreateProcess(port);
        Plug plug = new Plug(process, port, plugTitle, plugType, plugsMediator, internalPlugIndex, UiIndex, minElectricityVolt, maxElectricityVolt, true);
        AllStatisticsContainer StatisticsContainer = new AllStatisticsContainer(electricityUsageTillNow, lastSingleUsageStatistics, lastWeeklyStatistics, lastAnnualStatistics);
        plug.UpdateFieldsFromDB(overTimeFlag, isInvalidPlug, status, registeredToSleepMode, registeredToSafeMode, StatisticsContainer,
                invalidUsageVolt, avgElectricityUsageAfterLearning,learningUsages, learningTimes,finishedElectricityUsageLearning);

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

    public float getMinElectricityVolt() {
        return minElectricityVolt;
    }

    public void setMinElectricityVolt(int minElectricityVolt) {
        this.minElectricityVolt = minElectricityVolt;
    }

    public float getMaxElectricityVolt() {
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

    public boolean isRegisteredToSleepMode() {
        return registeredToSleepMode;
    }

    public void setRegisteredToSleepMode(boolean registeredToSleepMode) {
        this.registeredToSleepMode = registeredToSleepMode;
    }

    public boolean isRegisteredToSafeMode() {
        return registeredToSafeMode;
    }

    public void setRegisteredToSafeMode(boolean registeredToSafeMode) {
        this.registeredToSafeMode = registeredToSafeMode;
    }
}

