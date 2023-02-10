package com.workshop.mainserverworkshop.engine.modes;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;

public interface IModeListener {
    void handleMode(GenericMode eventMode);
}
