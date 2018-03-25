package de.embl.cba.cluster;

public class JobSettings
{
    public static final String DEFAULT_QUEUE = "htc";

    public int memoryPerJobInMegaByte = 16000;
    public int numWorkersPerNode = 4;
    public int timePerJobInMinutes = 60;
    public String queue = DEFAULT_QUEUE;
}
