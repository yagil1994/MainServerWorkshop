package com.workshop.mainserverworkshop.engine;

import com.workshop.mainserverworkshop.containers.AllStatisticsContainer;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;
import com.workshop.mainserverworkshop.engine.modes.IModeListener;
import com.workshop.mainserverworkshop.mediators.PlugsMediator;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Plug implements IModeListener, Comparable<Plug> {
    private Process process;
    private boolean status, overTimeFlag, isInvalidPlug;
    private String plugType, plugTitle;
    private float minElectricityVolt, maxElectricityVolt;
    private int port, internalPlugIndex, UiIndex, unitsToGetOld = 50, oldUnitsCounter = 1;
    private PlugsMediator plugsMediator;
    private ElectricityStorage electricityStorage;
    private Timer electricityConsumptionTimer, overTimeTimer;
    private boolean fakePlug;

    public Plug(Process i_Process, int i_port, String i_PlugTitle, String i_PlugType, PlugsMediator i_PlugsMediator, int i_InternalIndex, int i_UiIndex, int i_minElectricityVolt, int i_maxElectricityVolt) {
        starter(i_Process, i_port, i_PlugTitle, i_PlugType, i_PlugsMediator, i_InternalIndex, i_UiIndex, i_minElectricityVolt, i_maxElectricityVolt);
        initTimerAndElectricityConsumption();
        //plugsMediator.SavePlugToDB(this);
        plugsMediator.UpdateAllPlugsInDB();
    }

    //form PlugSave
    public Plug(Process i_Process, int i_port, String i_PlugTitle, String i_PlugType, PlugsMediator i_PlugsMediator, int i_InternalIndex, int i_UiIndex, float i_minElectricityVolt, float i_maxElectricityVolt, boolean fromPlugSave) {
        starter(i_Process, i_port, i_PlugTitle, i_PlugType, i_PlugsMediator, i_InternalIndex, i_UiIndex, i_minElectricityVolt, i_maxElectricityVolt);
    }

    private void starter(Process i_Process, int i_port, String i_PlugTitle, String i_PlugType, PlugsMediator i_PlugsMediator, int i_InternalIndex, int i_UiIndex, float i_minElectricityVolt, float i_maxElectricityVolt) {
        process = i_Process;
        plugType = i_PlugType;
        plugTitle = i_PlugTitle;
        minElectricityVolt = i_minElectricityVolt;
        maxElectricityVolt = i_maxElectricityVolt;
        port = i_port;
        plugsMediator = i_PlugsMediator;
        status = false;
        overTimeFlag = false;
        isInvalidPlug = false;
        fakePlug = i_UiIndex != 10;
        internalPlugIndex = i_InternalIndex;
        UiIndex = i_UiIndex;
        electricityStorage = new ElectricityStorage(i_minElectricityVolt, i_maxElectricityVolt, plugsMediator);
    }

    public void initTimerAndElectricityConsumption() {
        electricityConsumptionTimer = new Timer();
        overTimeTimer = new Timer();
        consumeElectricity();
    }

    public float getInvalidUsageVolt() {
        return electricityStorage.getInvalidUsageVolt();
    }

    public float getAvgElectricityUsageAfterLearning() {
        return electricityStorage.getAvgElectricityUsageAfterLearning();
    }

    public LinkedList<Float> getLearningUsages() {
        return electricityStorage.getLearningUsages();
    }

    public int getLearningTimes() {
        return electricityStorage.getLearningTimes();
    }

    public boolean isFinishedElectricityUsageLearning() {
        return electricityStorage.isFinishedElectricityUsageLearning();
    }

    public boolean isFakePlug() {
        return fakePlug;
    }

    public float getMinElectricityVolt() {
        return minElectricityVolt;
    }

    public float getMaxElectricityVolt() {
        return maxElectricityVolt;
    }

    private void consumeElectricity() {
        class Helper extends TimerTask {
            public void run() {
                if (status) {
                    electricityStorage.UpdateElectricityUsage(isInvalidPlug);
                    oldUnitsCounter = (oldUnitsCounter + 1) % unitsToGetOld;
                    if (oldUnitsCounter == 0)//every 50 times the timer works the device gets older
                    {
                        electricityStorage.LearnMoreAfterSomeTimePassed();
                    }
                }
            }
        }
        TimerTask updateElectricityUsageTimerTask = new Helper();
        electricityConsumptionTimer.schedule(updateElectricityUsageTimerTask, 1000, 1000);
    }

    public boolean isInvalidPlug() {
        return isInvalidPlug;
    }

    public void setFalseToInvalidAndTrueToValidThePlug(boolean i_Value) {
        synchronized (plugsMediator.GetInstance()) {
            isInvalidPlug = !i_Value;
        }

        //plugsMediator.SavePlugToDB(this);
        plugsMediator.UpdateAllPlugsInDB();
    }

    public float GetElectricityConsumptionTillNow() {
        float res = electricityStorage.getElectricityUsageTillNow();
        //plugsMediator.SavePlugToDB(this);
        plugsMediator.UpdateAllPlugsInDB();

        return res;
    }

    public int getInternalPlugIndex() {
        return internalPlugIndex;
    }

    public int getUiIndex() {
        return UiIndex;
    }

    public void updateUiIndex(int i_NewUiIndex) {
        if (isFakePlug()) {
            UiIndex = i_NewUiIndex;
            //plugsMediator.SavePlugToDB(this);
            plugsMediator.UpdateAllPlugsInDB();
        }
    }

    public float[] SimulateAnnualElectricityConsumption() {
        float[] annualElectricityConsumption = electricityStorage.SimulateAnnualElectricityStatisticsAndGetMonthList();
        return annualElectricityConsumption;
    }

    public float[] SimulateWeeklyElectricityConsumption() {
        float[] weeklyElectricityConsumption = electricityStorage.SimulateWeeklyElectricityStatisticsAndGetDayList();
        return weeklyElectricityConsumption;
    }

    public String off() {
        String res = "turned off";
        status = false;
        overTimeTimer.cancel();
        overTimeFlag = false;
        if (!fakePlug) {
            plugsMediator.RealPlugOnOrOff("off");
        } else if (process.isAlive()) {
            res = plugsMediator.sendTurnOnOrOffRequestToPlug(port, false);
        }

        //plugsMediator.SavePlugToDB(this);
        plugsMediator.UpdateAllPlugsInDB();

        return res;
    }

    public String on() {
        String res = "turned on";
        status = true;
        overTimeTimer = new Timer();
        if (!fakePlug) {
            plugsMediator.RealPlugOnOrOff("on");
        } else if (process.isAlive()) {
            res = plugsMediator.sendTurnOnOrOffRequestToPlug(port, true);
        }

        overTimeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                overTimeTimer.cancel();
                overTimeFlag = true;
                //plugsMediator.SavePlugToDB(plugsMediator.GetPlugAccordingToUiIndex(UiIndex));
                plugsMediator.UpdateAllPlugsInDB();
            }
        }, 5000, 5000);

        return res;
    }

    public void OverTimeAndDoNotTurnOff() {
        overTimeTimer.cancel();
        overTimeFlag = false;
        //plugsMediator.SavePlugToDB(this);
        plugsMediator.UpdateAllPlugsInDB();
    }


    public boolean isOverTimeFlag() {
        return overTimeFlag;
    }

    public Process getProcess() {
        return process;
    }

    public String getPlugType() {
        return plugType;
    }

    public String getPlugTitle() {
        return plugTitle;
    }

    public void updateStatus(boolean newStatus) {
        status = newStatus;
    }

    public String getOnOffStatus() {
        return status ? "on" : "off";
    }

    public boolean flipModeAndReturnPreviousMode() {
        updateStatus(!status);

        return !status;
    }

    @Override
    public void handleMode(GenericMode i_EventMode) {
        off();
    }

    public void stopTimer() {
        electricityConsumptionTimer.cancel();
    }

    public void KillProcess() {
        process.destroy();
    }

    public boolean getStatus() {
        return status;
    }

    public int getPort() {
        return port;
    }

    public AllStatisticsContainer getAllStatisticsContainer() {
        return new AllStatisticsContainer(electricityStorage.getElectricityUsageTillNow(),
                electricityStorage.getLastSingleUsageStatistics(), electricityStorage.getLastWeeklyStatistics(),
                electricityStorage.getLastAnnualStatistics());
    }

    public void UpdateFieldsFromDB(boolean overTimeFlag, boolean isInvalidPlug,
                                   boolean status, boolean registeredToSleepMode, boolean registeredToSafeMode,
                                   AllStatisticsContainer statisticsContainer,
                                   float invalidUsageVolt, float avgElectricityUsageAfterLearning,
                                   LinkedList<Float> learningUsages, int learningTimes,
                                   boolean finishedElectricityUsageLearning) {

        this.overTimeFlag = overTimeFlag;
        this.isInvalidPlug = isInvalidPlug;
        this.status = status;
        if (registeredToSleepMode && !plugsMediator.getPlugsThatRegisteredForMode(plugsMediator.SLEEP_MODE_LIST).contains(this)) {
            plugsMediator.addModeListener(this, plugsMediator.SLEEP_MODE_LIST);
        }
        if (registeredToSafeMode && !plugsMediator.getPlugsThatRegisteredForMode(plugsMediator.SAFE_MODE_LIST).contains(this)) {
            plugsMediator.addModeListener(this, plugsMediator.SAFE_MODE_LIST);
        }
        electricityStorage.setElectricityUsageTillNow(statisticsContainer.getElectricityUsageTillNow());
        electricityStorage.setLastSingleUsageStatistics(statisticsContainer.getLastSingleUsageStatistics());
        electricityStorage.setLastWeeklyStatistics(statisticsContainer.getLastWeeklyStatistics());
        electricityStorage.setLastAnnualStatistics(statisticsContainer.getLastAnnualStatistics());
        electricityStorage.setInvalidUsageVolt(invalidUsageVolt);
        electricityStorage.setAvgElectricityUsageAfterLearning(avgElectricityUsageAfterLearning);
        electricityStorage.setLearningUsages(learningUsages);
        electricityStorage.setLearningTimes(learningTimes);
        electricityStorage.setFinishedElectricityUsageLearning(finishedElectricityUsageLearning);
    }

    @Override
    public int compareTo(Plug other) {
        if (this.getUiIndex() < other.getUiIndex())
            return -1;
        else if (this.getUiIndex() > other.getUiIndex())
            return 1;
        return 0;
    }
}
