package com.workshop.mainserverworkshop.engine;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;
import com.workshop.mainserverworkshop.engine.modes.IModeListener;
import com.workshop.mainserverworkshop.mediators.PlugsMediator;
import java.util.Timer;
import java.util.TimerTask;

public class Plug implements IModeListener {
    private Process process;
    private boolean status, overTimeFlag;
    private String plugType, plugTitle;
    private int port, internalPlugIndex, UiIndex, minElectricityVolt, maxElectricityVolt;
    private PlugsMediator plugsMediator;
    private ElectricityStorage electricityStorage;
    private Timer electricityConsumptionTimer, overTimeTimer;

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
        internalPlugIndex = i_InternalIndex;
        UiIndex = i_UiIndex;
        electricityStorage = new ElectricityStorage(i_minElectricityVolt, i_maxElectricityVolt);
        electricityConsumptionTimer = new Timer();
        overTimeTimer = new Timer();
        consumeElectricity();
    }

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
                  electricityStorage.UpdateElectricityUsageAndGetUpdatedValue();
                }
            }
        }
        TimerTask updateProcessTable = new Helper();
        electricityConsumptionTimer.schedule(updateProcessTable,1000, 1000);
    }

    public float GetElectricityConsumptionTillNow()
    {
       return electricityStorage.getElectricityUsageTillNow();
    }

    public int getInternalPlugIndex() {
        return internalPlugIndex;
    }

    public int getUiIndex() {return UiIndex;}
    public void updateUiIndex(int i_NewUiIndex) {UiIndex = i_NewUiIndex;}

    public float[] SimulateAnnualElectricityConsumption() {
        return electricityStorage.SimulateAnnualElectricityStatisticsAndGetMonthList();
    }

    public String off() {
        status = false;
        overTimeTimer.cancel();
        overTimeFlag = false;

        return plugsMediator.sendTurnOnOrOffRequestToPlug(port, false);
    }

    public String on() {
        status = true;
        overTimeTimer = new Timer();
        overTimeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                overTimeTimer.cancel();
                overTimeFlag = true;
            }
        }, 5000, 5000);

        return plugsMediator.sendTurnOnOrOffRequestToPlug(port, true);
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

    public void stopTimer(){
        electricityConsumptionTimer.cancel();
    }

    public void KillProcess()
    {
        process.destroy();
    }
}
