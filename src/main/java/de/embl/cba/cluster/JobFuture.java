package de.embl.cba.cluster;

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

    public static final String STD_OUT = "StdOut";
    public static final String STD_ERR = "StdErr";


    public JobFuture( SSHExecutorService executorService, long jobID )
    {
        this.executorService = executorService;
        this.jobID = jobID;
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

    public boolean isRunning()
    {
        if ( executorService.isStarted( jobID ) && ! executorService.isDone( jobID ) )
        {
            Utils.logger.info( "Job " + jobID + " is running." );
            return true;
        }
        else
        {
            Utils.logger.info( "Job " + jobID + " has not started yet." );
            return false;
        }
    }

    public boolean isDone()
    {
        return executorService.isDone( jobID );
    }

    public String getError()
    {
        return executorService.getJobError( jobID );
    }

    public String getOutput()
    {
        Utils.logger.info( "Fetching " + jobID + "'s current output..." );
        return executorService.getJobOutput( jobID );
    }

    public HashMap< String, Object > get() throws InterruptedException, ExecutionException
    {
        HashMap< String, Object > results = new HashMap<>(  );

        while ( ! executorService.isDone( jobID ) )
        {
            Utils.logger.info( "Job " + jobID + " is not done yet." );
            Thread.sleep( 10000 );
        }

        Utils.logger.info( "Job " + jobID + " has finished." );

        results.put( STD_OUT, executorService.getJobOutput( jobID ) );

        return results;
    }


    public Object get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
    {
        return null;
    }

}
