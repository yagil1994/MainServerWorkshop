package com.workshop.mainserverworkshop.engine;
import com.workshop.mainserverworkshop.engine.modes.SleepMode;

public class Plug implements ISleepModeListener{
    private Process process;
    private boolean status;
    private String plugName;
    private int port;
    private int plugIndex;
    private Plugs_Mediator plugsMediator;

    public Plug(Process i_Process, int i_port, String i_PlugName, Plugs_Mediator i_PlugsMediator,int i_PlugIndex)
    {
        process = i_Process;
        plugName = i_PlugName;
        port = i_port;
        plugsMediator = i_PlugsMediator;
        status = false;
        plugIndex = i_PlugIndex;
    }

    public int getPlugIndex() {
        return plugIndex;
    }

    public String off()
    {
        status = false;

        return plugsMediator.sendTurnOnOrOffRequestToPlug(port, false);
    }

    public String on()
    {
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

    public void updateStatus(boolean newStatus)
    {
        status = newStatus;
    }
    public String getOnOffStatus() {
        return status ? "on" :"off";
    }

    public boolean flipModeAndReturnPreviousMode()
    {
       updateStatus(!status);

        return !status;
    }

    @Override
    public void handleSleepMode(SleepMode sleepEvent) {
        off();
    }
}
