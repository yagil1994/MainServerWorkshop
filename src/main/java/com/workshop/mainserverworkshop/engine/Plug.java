package com.workshop.mainserverworkshop.engine;

public class Plug {
    private Process process;
    private String plugName;
    private int indexInList;
    private int port;
    private DevicesMediator devicesMediator;

    public Plug(Process i_Process, int i_port, String i_PlugName, DevicesMediator i_DevicesMediator)
    {
        process = i_Process;
        plugName = i_PlugName;
        port = i_port;
        devicesMediator = i_DevicesMediator;
    }

    public String off()
    {
        return devicesMediator.sendTurOffRequestToPlug(port);
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
}
