package com.workshop.mainserverworkshop.engine;

import java.util.ArrayList;
import java.util.List;

public class ElectricityStorage {
    private int lastConsumption;
    private final List<Integer> monthConsumption;

    public ElectricityStorage() {
       monthConsumption = new ArrayList(12);
       lastConsumption = 0;
    }

    public int GetLastConsumption() {
        return lastConsumption;
    }

    public void GetLastConsumption(int lastConsumption) {
        this.lastConsumption = lastConsumption;
    }

    public int GetMonthConsumption(Month month) {return monthConsumption.get(month.getValue());}

    public int AddToMonthConsumption(Month month, int ConsumptionToAdd) {
        return monthConsumption.set(month.getValue(),monthConsumption.get(month.getValue()) + ConsumptionToAdd);}

    public boolean IsLastConsumptionValid(int validElectricityConsumption, int deviation) {
        int min = validElectricityConsumption + deviation;
        int max = validElectricityConsumption - deviation;
        return ((lastConsumption < max) && (lastConsumption > min));
    }
}
