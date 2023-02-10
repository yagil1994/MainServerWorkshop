package com.workshop.mainserverworkshop.engine;

import com.workshop.mainserverworkshop.engine.modes.GenericMode;
import com.workshop.mainserverworkshop.engine.modes.IModeListener;
import com.workshop.mainserverworkshop.mediators.PlugsMediator;

import java.util.Timer;
import java.util.TimerTask;

public class Plug implements IModeListener {
    private Process process;
    private boolean status;
    private String plugName;
    private int port;
    private int plugIndex;
    private PlugsMediator plugsMediator;
    private ElectricityStorage electricityStorage;
    private Timer electricityConsumptionTimer;

    public Plug(Process i_Process, int i_port, String i_PlugName, PlugsMediator i_PlugsMediator, int i_PlugIndex, int i_minElectricityVolt, int i_maxElectricityVolt) {
        process = i_Process;
        plugName = i_PlugName;
        port = i_port;
        plugsMediator = i_PlugsMediator;
        status = false;
        plugIndex = i_PlugIndex;
        electricityStorage = new ElectricityStorage(i_minElectricityVolt, i_maxElectricityVolt);
        electricityConsumptionTimer = new Timer();
        consumeElectricity();
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

    public int getPlugIndex() {
        return plugIndex;
    }

    public float[] SimulateAnnualElectricityConsumption()
    {
        return electricityStorage.SimulateAnnualElectricityStatisticsAndGetMonthList();
    }

    public String off() {
        status = false;

        return plugsMediator.sendTurnOnOrOffRequestToPlug(port, false);
    }

    public String on() {
        status = true;

        return plugsMediator.sendTurnOnOrOffRequestToPlug(port, true);
    }

    public Process getProcess() {
        return process;
    }

    public String getPlugName() {
        return plugName;
    }

    public int getPort() {
        return port;
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
    public void handleMode(GenericMode eventMode) {
        off();
    }
}
