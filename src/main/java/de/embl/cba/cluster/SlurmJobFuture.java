package de.embl.cba.cluster;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SlurmJobFuture implements Future
{
    SlurmJobScript jobScript;
    SlurmExecutorService executorService;
    long jobID;

    public SlurmJobFuture( SlurmExecutorService executorService, SlurmJobScript jobScript, long jobID )
    {
        this.executorService = executorService;
        this.jobScript = jobScript;
        this.jobID = jobID;
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
        executorService.jobStatus( jobID );
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
        return jobScript.getJobText();
    }
}
