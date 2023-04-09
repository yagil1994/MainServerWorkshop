package com.workshop.mainserverworkshop.engine;
import com.workshop.mainserverworkshop.containers.AllStatisticsContainer;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;
import com.workshop.mainserverworkshop.engine.modes.IModeListener;
import com.workshop.mainserverworkshop.mediators.PlugsMediator;

import java.util.Timer;
import java.util.TimerTask;

public class Plug implements IModeListener {
    private Process process;
    private boolean status, overTimeFlag,isInvalidPlug;
    private String plugType, plugTitle;
    private int port, internalPlugIndex, UiIndex, minElectricityVolt, maxElectricityVolt;
    private PlugsMediator plugsMediator;
    private ElectricityStorage electricityStorage;
    private Timer electricityConsumptionTimer, overTimeTimer;
    private boolean fakePlug;

    public Plug(Process i_Process, int i_port,String i_PlugTitle, String i_PlugType, PlugsMediator i_PlugsMediator,int i_InternalIndex, int i_UiIndex, int i_minElectricityVolt, int i_maxElectricityVolt) {
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
        fakePlug = true; //change it if real
        internalPlugIndex = i_InternalIndex;
        UiIndex = i_UiIndex;
        electricityStorage = new ElectricityStorage(i_minElectricityVolt, i_maxElectricityVolt);
        electricityConsumptionTimer = new Timer();
        overTimeTimer = new Timer();
        consumeElectricity();
        plugsMediator.SavePlugToDB(this);
    }

    public boolean isFakePlug() {return fakePlug;}

    public int getMinElectricityVolt() {
        return minElectricityVolt;
    }

    public int getMaxElectricityVolt() {
        return maxElectricityVolt;
    }

    private void consumeElectricity()
    {
        class Helper extends TimerTask
        {
            public void run()
            {
                if(status){
                    float electricityConsumptionInLiveForSingleUsage = GetElectricityConsumptionInLiveForSingleUsage();
                  electricityStorage.UpdateElectricityUsageAndGetUpdatedValue(electricityConsumptionInLiveForSingleUsage,isInvalidPlug);
                }
            }
        }
        TimerTask updateProcessTable = new Helper();
        electricityConsumptionTimer.schedule(updateProcessTable,1000, 1000);
    }

    public boolean isInvalidPlug() {
        return isInvalidPlug;
    }

    public void setFalseToInvalidAndTrueToValidThePlug(boolean i_Value) {
        isInvalidPlug = !i_Value;
        plugsMediator.SavePlugToDB(this);
    }

    public float GetElectricityConsumptionTillNow()
    {
       float res = electricityStorage.getElectricityUsageTillNow();
       plugsMediator.SavePlugToDB(this);
       return res;
    }

    public int getInternalPlugIndex() {
        return internalPlugIndex;
    }

    public int getUiIndex() {return UiIndex;}

    public void updateUiIndex(int i_NewUiIndex) {
        UiIndex = i_NewUiIndex;
        plugsMediator.SavePlugToDB(this);
    }

    public float[] SimulateAnnualElectricityConsumption() {
        float[] annualElectricityConsumption = electricityStorage.SimulateAnnualElectricityStatisticsAndGetMonthList();
        plugsMediator.SavePlugToDB(this);
        return annualElectricityConsumption;
    }

    public float[] SimulateWeeklyElectricityConsumption() {
        float[] weeklyElectricityConsumption = electricityStorage.SimulateWeeklyElectricityStatisticsAndGetDayList();
        plugsMediator.SavePlugToDB(this);
        return weeklyElectricityConsumption;
    }

    public float GetElectricityConsumptionInLiveForSingleUsage() {
        float res = !isInvalidPlug ? electricityStorage.getLastSingleUsageStatistics() : maxElectricityVolt*2;
        plugsMediator.SavePlugToDB(this);

        return res;
    }

    public String off() {
        status = false;
        overTimeTimer.cancel();
        overTimeFlag = false;
        String res = "turned off";
        if(process.isAlive()){
            res = plugsMediator.sendTurnOnOrOffRequestToPlug(port, false);
        }
        plugsMediator.SavePlugToDB(this);

        return res;
    }

    public String on() {
        status = true;
        overTimeTimer = new Timer();
        overTimeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                overTimeTimer.cancel();
                overTimeFlag = true;
                plugsMediator.SavePlugToDB(plugsMediator.GetPlugAccordingToUiIndex(UiIndex));
            }
        }, 5000, 5000);

        String res = "turned on";
        if(process.isAlive()){
            res = plugsMediator.sendTurnOnOrOffRequestToPlug(port,true);
        }
        plugsMediator.SavePlugToDB(this);

        return res;
    }

    public void OverTimeAndDoNotTurnOff(){
        overTimeTimer.cancel();
        overTimeFlag = false;
        plugsMediator.SavePlugToDB(this);
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
        plugsMediator.SavePlugToDB(this);
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

    public void stopTimer(){
        electricityConsumptionTimer.cancel();
    }

    public void KillProcess()
    {
        process.destroy();
    }

    public boolean getStatus() {
        return status;
    }

    public int getPort() {
        return port;
    }

    public AllStatisticsContainer getAllStatisticsContainer(){
        return new AllStatisticsContainer(electricityStorage.getElectricityUsageTillNow(),
                electricityStorage.getLastSingleUsageStatistics(),electricityStorage.getLastWeeklyStatistics(),
                electricityStorage.getLastAnnualStatistics());
    }

    public void UpdateFieldsFromDB(boolean overTimeFlag, boolean isInvalidPlug, boolean status, boolean registeredToSleepMode, boolean registeredToSafeMode, AllStatisticsContainer statisticsContainer){
        this.overTimeFlag = overTimeFlag;
        this.isInvalidPlug = isInvalidPlug;
        this.status = status;
        if(registeredToSleepMode && !plugsMediator.getPlugsThatRegisteredForMode(plugsMediator.SLEEP_MODE_LIST).contains(this)){
                plugsMediator.addModeListener(this, plugsMediator.SLEEP_MODE_LIST);
        }
        if(registeredToSafeMode && !plugsMediator.getPlugsThatRegisteredForMode(plugsMediator.SAFE_MODE_LIST).contains(this)){
            plugsMediator.addModeListener(this, plugsMediator.SAFE_MODE_LIST);
        }
        electricityStorage.setElectricityUsageTillNow(statisticsContainer.getElectricityUsageTillNow());
        electricityStorage.setLastSingleUsageStatistics(statisticsContainer.getLastSingleUsageStatistics());
        electricityStorage.setLastWeeklyStatistics(statisticsContainer.getLastWeeklyStatistics());
        electricityStorage.setLastAnnualStatistics(statisticsContainer.getLastAnnualStatistics());
    }
}
