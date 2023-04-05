package com.workshop.mainserverworkshop.containers;

public class AllStatisticsContainer {
    private float electricityUsageTillNow, lastSingleUsageStatistics;
    private float[] lastWeeklyStatistics, lastAnnualStatistics;

    public AllStatisticsContainer(float electricityUsageTillNow, float lastSingleUsageStatistics, float[] lastWeeklyStatistics, float[] lastAnnualStatistics) {
        this.electricityUsageTillNow = electricityUsageTillNow;
        this.lastSingleUsageStatistics = lastSingleUsageStatistics;
        this.lastWeeklyStatistics = lastWeeklyStatistics;
        this.lastAnnualStatistics = lastAnnualStatistics;
    }

    public float getElectricityUsageTillNow() {
        return electricityUsageTillNow;
    }

    public void setElectricityUsageTillNow(float electricityUsageTillNow) {
        this.electricityUsageTillNow = electricityUsageTillNow;
    }

    public float getLastSingleUsageStatistics() {
        return lastSingleUsageStatistics;
    }

    public void setLastSingleUsageStatistics(float lastSingleUsageStatistics) {
        this.lastSingleUsageStatistics = lastSingleUsageStatistics;
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
}
