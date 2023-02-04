package com.workshop.mainserverworkshop.engine;

public class Plug {
    private Process process;
    private boolean status;
    private String plugName;
    private int indexInList;
    private int port;
    private PlugsMediator devicesMediator;

    public Plug(Process i_Process, int i_port, String i_PlugName, PlugsMediator i_DevicesMediator)
    {
        process = i_Process;
        plugName = i_PlugName;
        port = i_port;
        devicesMediator = i_DevicesMediator;
        status = false;
    }

    public String off()
    {
        status = false;
        return devicesMediator.sendTurnOffRequestToPlug(port);
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
}
