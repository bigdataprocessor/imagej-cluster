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
    public static final String NO_EVERYTHING_FINE = "No resubmission needed everything seems fine!";

    public static final String XVFB_ERROR = "/usr/bin/xvfb-run";
    public static final String SLURM_STEP_ERROR = "slurmstepd";



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

    public String needsResubmission()
    {
        String err = executorService.getJobError( jobID );

        if ( err.contains( XVFB_ERROR ) )
        {
            return "xvfb_error";
        }

        if ( err.contains( SLURM_STEP_ERROR ) )
        {
            return "slurm_step_error";
        }

        return NO_EVERYTHING_FINE;
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
