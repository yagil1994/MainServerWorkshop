package com.workshop.mainserverworkshop.engine;
import com.workshop.mainserverworkshop.engine.modes.GenericMode;

public interface IModeListener {
    void handleMode(GenericMode eventMode);
}
