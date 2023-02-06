package com.workshop.mainserverworkshop.engine;

import com.workshop.mainserverworkshop.mediators.Plugs_Mediator;

public class Plug {
    private Process process;
    private boolean status;
    private String plugName;
    private int indexInList;
    private int port;
    private Plugs_Mediator plugsMediator;

    public Plug(Process i_Process, int i_port, String i_PlugName, Plugs_Mediator i_PlugsMediator)
    {
        process = i_Process;
        plugName = i_PlugName;
        port = i_port;
        plugsMediator = i_PlugsMediator;
        status = false;
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

    public boolean isOn() {
        return status;
    }

    public boolean flipModeAndReturnPreviousMode()
    {
       updateStatus(!status);

        return !status;
    }
}
