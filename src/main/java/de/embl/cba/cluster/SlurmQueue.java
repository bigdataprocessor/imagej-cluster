package de.embl.cba.cluster;

public abstract class SlurmQueue
{
    public static final String DEFAULT_QUEUE = "htc";
    public static final String BIGMEM_QUEUE = "bigmem";
    public static final String ONE_DAY_QUEUE = "1day";
    public static final String ONE_WEEK_QUEUE = "1week";
    public static final String ONE_MONTH_QUEUE = "1month";
    public static final String GPU_QUEUE = "gpu";
}
