package com.workshop.mainserverworkshop.containers;

public class IndexAndStatisticsContainer
{
    private String index, statistics;

    public IndexAndStatisticsContainer(String i_Index, String i_Statistics)
    {
        index = i_Index;
        statistics = i_Statistics;
    }

    public String getIndex() {
        return index;
    }

    public String getStatistics() {
        return statistics;
    }
}
