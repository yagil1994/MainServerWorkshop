package com.workshop.mainserverworkshop.engine;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Random;

public class ElectricityStorage {
    private float minElectricityVolt, maxElectricityVolt;
    private float electricityUsageTillNow, lastSingleUsageStatistics, invalidUsageVolt, avgElectricityUsageAfterLearning;
    private float[] lastWeeklyStatistics, lastAnnualStatistics;
    private LinkedList<Float> learningUsages;
    private int learningTimes;
    private boolean finishedElectricityUsageLearning;

    public float getInvalidUsageVolt() {
        return invalidUsageVolt;
    }

    public float getAvgElectricityUsageAfterLearning() {
        return avgElectricityUsageAfterLearning;
    }

    public LinkedList<Float> getLearningUsages() {
        return learningUsages;
    }

    public int getLearningTimes() {
        return learningTimes;
    }

    public boolean isFinishedElectricityUsageLearning() {
        return finishedElectricityUsageLearning;
    }

    public void setInvalidUsageVolt(float invalidUsageVolt) {
        this.invalidUsageVolt = invalidUsageVolt;
    }

    public void setAvgElectricityUsageAfterLearning(float avgElectricityUsageAfterLearning) {
        this.avgElectricityUsageAfterLearning = avgElectricityUsageAfterLearning;
    }

    public void setLearningUsages(LinkedList<Float> learningUsages) {
        this.learningUsages = learningUsages;
    }

    public void setLearningTimes(int learningTimes) {
        this.learningTimes = learningTimes;
    }

    public void setFinishedElectricityUsageLearning(boolean finishedElectricityUsageLearning) {
        this.finishedElectricityUsageLearning = finishedElectricityUsageLearning;
    }

    public ElectricityStorage(float i_MinElectricityVoltInput, float i_MaxElectricityVoltInput) {
        minElectricityVolt = i_MinElectricityVoltInput;
        maxElectricityVolt = i_MaxElectricityVoltInput;
        electricityUsageTillNow = 0f;
        finishedElectricityUsageLearning = false;
        invalidUsageVolt = 0f; //we can get the invalidation info only after the learning is finished
        avgElectricityUsageAfterLearning = 0;
        learningTimes = 10;
        learningUsages = new LinkedList<>();
    }

   synchronized public float[] SimulateAnnualElectricityStatisticsAndGetMonthList() {
        float[] electricityConsumption = new float[12];
        Random random = new Random();
        for (int i = 0; i < electricityConsumption.length; i++) { //(Wattage × Hours Used Per Day) ÷ 1000 = Daily Kilowatt-hour (kWh) consumption
            float randomVolt = random.nextFloat(maxElectricityVolt - minElectricityVolt + 1) + minElectricityVolt;
            int randomNumberOfUsageInDay = random.nextInt(25);
            electricityConsumption[i] = ((randomVolt * randomNumberOfUsageInDay) / 1000f) * 30;
        }

        lastAnnualStatistics = electricityConsumption;

        return electricityConsumption;
    }

    synchronized public float[] SimulateWeeklyElectricityStatisticsAndGetDayList() {
        float[] dailyElectricityConsumption = new float[7];
        Random random = new Random();
        for (int i = 0; i < dailyElectricityConsumption.length; i++) { //(Wattage × Hours Used Per Day) ÷ 1000 = Daily Kilowatt-hour (kWh) consumption
            float randomVolt = random.nextFloat(maxElectricityVolt - minElectricityVolt + 1) + minElectricityVolt;
            int randomNumberOfUsageInDay = random.nextInt(25);
            dailyElectricityConsumption[i] = ((randomVolt * randomNumberOfUsageInDay) / 1000f);
        }

        lastWeeklyStatistics = dailyElectricityConsumption;

        return dailyElectricityConsumption;
    }

     public void UpdateElectricityUsage(boolean isInvalid) {
        Random random = new Random();
        float randomVolt = random.nextFloat(maxElectricityVolt - minElectricityVolt + 1) + minElectricityVolt;
        int randomNumberOfUsageInDay = random.nextInt(25);
        float add = (((randomVolt * randomNumberOfUsageInDay) / 1000f) / 86);

        if (isInvalid && finishedElectricityUsageLearning) {
            add = SimulateInValidConsumption();
        }

        synchronized (this) {
            electricityUsageTillNow += add;
            lastSingleUsageStatistics = add;
        }

        if (!finishedElectricityUsageLearning) {
            LearnElectricityUsage(add);
        }
    }

    synchronized public void LearnMoreAfterSomeTimePassed() {
        learningUsages.clear();
        finishedElectricityUsageLearning = false;
        maxElectricityVolt *= 1.05;
        minElectricityVolt *= 1.05;
        avgElectricityUsageAfterLearning = 0f;
    }

    synchronized void LearnElectricityUsage(float add) {
        if (learningUsages.size() < learningTimes) {
            learningUsages.add(add);
        }

        if (learningUsages.size() == learningTimes) {
            avgElectricityUsageAfterLearning = calculateAgvConsumption();
            invalidUsageVolt = (float) (avgElectricityUsageAfterLearning * (1.3));
            finishedElectricityUsageLearning = true;
        }
    }

    public float SimulateInValidConsumption() {

        return invalidUsageVolt;
    }

    public float calculateAgvConsumption() {
        float sum = 0;
        for (float usage : learningUsages) {
            sum += usage;
        }
        return sum / (float) learningUsages.size();
    }

    public float getElectricityUsageTillNow() {
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
