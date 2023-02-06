package com.workshop.mainserverworkshop.engine;
import com.workshop.mainserverworkshop.engine.modes.SleepMode;
import jdk.jfr.Event;

public interface ISleepModeListener {
    void handleSleepMode(SleepMode sleepEvent);
}
