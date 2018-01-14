package de.embl.cba.cluster;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SlurmJobFuture implements Future
{
    ImageJGroovyScriptJob imageJGroovyScriptJob;
    SlurmExecutorService executorService;
    long jobID;

    public SlurmJobFuture( SlurmExecutorService executorService, ImageJGroovyScriptJob imageJGroovyScriptJob, long jobID )
    {
        this.executorService = executorService;
        this.imageJGroovyScriptJob = imageJGroovyScriptJob;
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

}
