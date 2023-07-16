package com.workshop.mainserverworkshop.engine;
import java.text.DecimalFormat;
import java.util.Random;

public class ElectricityStorage {
    private int minElectricityVolt, maxElectricityVolt;
    private float electricityUsageTillNow, lastSingleUsageStatistics;
    private float[] lastWeeklyStatistics, lastAnnualStatistics;

    public ElectricityStorage(int i_MinElectricityVoltInput, int i_MaxElectricityVoltInput) {
        minElectricityVolt = i_MinElectricityVoltInput;
        maxElectricityVolt = i_MaxElectricityVoltInput;
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

        lastAnnualStatistics = electricityConsumption;

        return electricityConsumption;
    }

    public float[] SimulateWeeklyElectricityStatisticsAndGetDayList() {
        float[] dailyElectricityConsumption = new float[7];
        Random random = new Random();
        for (int i = 0; i < dailyElectricityConsumption.length; i++) { //(Wattage × Hours Used Per Day) ÷ 1000 = Daily Kilowatt-hour (kWh) consumption
            int randomVolt = random.nextInt(maxElectricityVolt - minElectricityVolt + 1) + minElectricityVolt;
            int randomNumberOfUsageInDay = random.nextInt(25);
            dailyElectricityConsumption[i] = ((randomVolt * randomNumberOfUsageInDay) / 1000f);
        }

        lastWeeklyStatistics = dailyElectricityConsumption;

        return dailyElectricityConsumption;
    }

    public void UpdateElectricityUsage(boolean isInvalid)
    {
        Random random = new Random();
        int randomVolt = random.nextInt(maxElectricityVolt - minElectricityVolt + 1) + minElectricityVolt;
        int randomNumberOfUsageInDay = random.nextInt(25);
        float add = (((randomVolt * randomNumberOfUsageInDay) / 1000f)/86);

        if(isInvalid){
            add = SimulateInValidConsumption();
        }

        synchronized (this){
            electricityUsageTillNow += add;
            lastSingleUsageStatistics = add;
        }
    }

    public float SimulateInValidConsumption(){
        return  (520*2)/ 100f/86;   //=0.120930225
    }

    public float getElectricityUsageTillNow(){
        DecimalFormat df = new DecimalFormat("#.####");
        return Float.parseFloat(df.format(electricityUsageTillNow));
    }

    public void setElectricityUsageTillNow(float electricityUsageTillNow) {
        this.electricityUsageTillNow = electricityUsageTillNow;
    }

    public float[] getLastWeeklyStatistics() {
        return lastWeeklyStatistics;
    }

    public void setLastWeeklyStatistics(float[] lastWeeklyStatistics) {
        this.lastWeeklyStatistics = lastWeeklyStatistics;
    }

    public float[] getLastAnnualStatistics() {
        return lastAnnualStatistics;
    }

    public void setLastAnnualStatistics(float[] lastAnnualStatistics) {
        this.lastAnnualStatistics = lastAnnualStatistics;
    }

    public float getLastSingleUsageStatistics() {
        return lastSingleUsageStatistics;
    }

    public void setLastSingleUsageStatistics(float lastSingleUsageStatistics) {
        this.lastSingleUsageStatistics = lastSingleUsageStatistics;
    }
}
