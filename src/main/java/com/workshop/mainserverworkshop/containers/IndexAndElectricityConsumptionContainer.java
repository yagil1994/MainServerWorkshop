package com.workshop.mainserverworkshop.containers;

public class IndexAndElectricityConsumptionContainer
{
    private String index, ElectricityConsumption;

    public IndexAndElectricityConsumptionContainer(String i_Index, String i_ElectricityConsumption)
    {
        index = i_Index;
        ElectricityConsumption = i_ElectricityConsumption;
    }

    public String getIndex() {
        return index;
    }

    public String getStatistics() {
        return ElectricityConsumption;
    }
}
