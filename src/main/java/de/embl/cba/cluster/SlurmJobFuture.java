package de.embl.cba.cluster;

import de.embl.cba.cluster.job.SlurmJob;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SlurmJobFuture implements Future
{
    SlurmJob slurmJob;
    SlurmExecutorService executorService;
    long jobID;

    public SlurmJobFuture( SlurmExecutorService executorService, SlurmJob slurmJob, long jobID )
    {
        this.executorService = executorService;
        this.slurmJob = slurmJob;
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
        executorService.checkJobStatus( jobID );
        return false;
    }

    public String getError() throws IOException
    {
        return executorService.readJobOutput( jobID );
    }
    
    public String getOutput() throws IOException
    {
        return executorService.readJobOutput( jobID );
    }


    public String status()
    {
        return executorService.checkJobStatus( jobID );
    }

    public Object get() throws InterruptedException, ExecutionException
    {
        return null;
    }

    public Object get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
    {
        return null;
    }

}
