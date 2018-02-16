package de.embl.cba.cluster;

import de.embl.cba.cluster.job.JobScript;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JobFuture implements Future
{
    SSHExecutorService executorService;
    long jobID;
    JobScript jobScript;

    public static final String STD_OUT = "StdOut";
    public static final String STD_ERR = "StdErr";


    public JobFuture( SSHExecutorService executorService, long jobID, JobScript jobScript )
    {
        this.executorService = executorService;
        this.jobID = jobID;
        this.jobScript = jobScript;
    }

    public boolean cancel( boolean mayInterruptIfRunning )
    {
        // TODO
        return false;
    }

    public boolean isCancelled()
    {
        // TODO
        return false;
    }

    public long getJobID()
    {
        return jobID;
    }

    public boolean isStarted()
    {
        if ( executorService.isStarted( jobID ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isDone()
    {
        return executorService.isDone( jobID );
    }

    public boolean needsResubmission()
    {
        String err = executorService.getJobError( jobID );

        if ( err.contains( "/usr/bin/xvfb-run" ) )
        {
            return true;
        }

        return false;
    }

    public void resubmit()
    {
        executorService.submit( jobScript, jobID );
    }


    public String getError()
    {
        return executorService.getJobError( jobID );
    }

    public String getOutput()
    {
        return executorService.getJobOutput( jobID );
    }



    public HashMap< String, Object > get() throws InterruptedException, ExecutionException
    {
        HashMap< String, Object > results = new HashMap<>(  );

        while ( ! executorService.isDone( jobID ) )
        {
            Thread.sleep( 10000 );
        }

        results.put( STD_OUT, executorService.getJobOutput( jobID ) );

        return results;
    }


    public Object get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
    {
        return null;
    }

}
