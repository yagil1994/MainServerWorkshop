package com.workshop.mainserverworkshop.engine;
import java.text.DecimalFormat;
import java.util.Random;

public class ElectricityStorage {
    private int minElectricityVolt, maxElectricityVolt;
    private float electricityUsageTillNow;

    public ElectricityStorage(int i_MinElectricityVoltInput, int i_MxElectricityVoltInput) {
        minElectricityVolt = i_MinElectricityVoltInput;
        maxElectricityVolt = i_MxElectricityVoltInput;
        electricityUsageTillNow = 0f;
    }

    public float[] SimulateAnnualElectricityStatisticsAndGetMonthList() {
        float[] electricityConsumption = new float[12];
        Random random = new Random();
        for (int i = 0; i < electricityConsumption.length; i++) { //(Wattage × Hours Used Per Day) ÷ 1000 = Daily Kilowatt-hour (kWh) consumption
            int randomVolt = random.nextInt(maxElectricityVolt - minElectricityVolt + 1) + minElectricityVolt;
            int randomNumberOfUsageInDay = random.nextInt(25);
            electricityConsumption[i] = ((randomVolt * randomNumberOfUsageInDay) / 1000f) * 30;
        }

        return electricityConsumption;
    }

    public float GetElectricityConsumptionInLiveForSingleUsage()
    {
        Random random = new Random();

        return random.nextInt(maxElectricityVolt - minElectricityVolt + 1) + minElectricityVolt;
    }

    public float[] SimulateWeeklyElectricityStatisticsAndGetDayList() {
        float[] dailyElectricityConsumption = new float[7];
        Random random = new Random();
        for (int i = 0; i < dailyElectricityConsumption.length; i++) { //(Wattage × Hours Used Per Day) ÷ 1000 = Daily Kilowatt-hour (kWh) consumption
            int randomVolt = random.nextInt(maxElectricityVolt - minElectricityVolt + 1) + minElectricityVolt;
            int randomNumberOfUsageInDay = random.nextInt(25);
            dailyElectricityConsumption[i] = ((randomVolt * randomNumberOfUsageInDay) / 1000f);
        }

        return dailyElectricityConsumption;
    }

    public void UpdateElectricityUsageAndGetUpdatedValue()
    {
        Random random = new Random();
        int randomVolt = random.nextInt(maxElectricityVolt - minElectricityVolt + 1) + minElectricityVolt;
        int randomNumberOfUsageInDay = random.nextInt(25);
        float add = (((randomVolt * randomNumberOfUsageInDay) / 1000f)/86);

        synchronized (this){
            electricityUsageTillNow += add;
        }
    }

    public float getElectricityUsageTillNow(){
        DecimalFormat df = new DecimalFormat("#.####");
        return Float.parseFloat(df.format(electricityUsageTillNow));
    }
}
