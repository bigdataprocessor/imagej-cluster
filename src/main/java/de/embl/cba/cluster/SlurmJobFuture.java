package de.embl.cba.cluster;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SlurmJobFuture implements Future
{
    SlurmJobScript slurmJobScript;

    public SlurmJobFuture( SlurmJobScript slurmJobScript )
    {
        this.slurmJobScript = slurmJobScript;
    }

    public boolean cancel( boolean mayInterruptIfRunning )
    {
        return false;
    }

    public boolean isCancelled()
    {
        return false;
    }

    public boolean isDone()
    {
        return false;
    }

    public Object get() throws InterruptedException, ExecutionException
    {
        return null;
    }

    public Object get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
    {
        return null;
    }

    public String getJobText()
    {
        slurmJobScript.getJobText()
    }
}
