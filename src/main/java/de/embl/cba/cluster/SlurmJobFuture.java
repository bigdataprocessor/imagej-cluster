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
        // TODO
        return false;
    }

    public boolean isCancelled()
    {
        String status = executorService.getJobStatus( jobID );

        if ( status.equals( SlurmJobStatus.CANCELLED ) )
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
        String status = executorService.getJobStatus( jobID );

        if ( status.equals( SlurmJobStatus.COMPLETED ) )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public String getError()
    {
        String error;
        try
        {
            error = executorService.readJobError( jobID );
        }
        catch ( IOException e )
        {
            return e.toString();
        }
        return error;
    }

    public String getOutput()
    {
        String output;
        try
        {
            output = executorService.readJobOutput( jobID );
        }
        catch ( IOException e )
        {
            return e.toString();
        }

        return output;
    }

    public String getStatus()
    {
        return executorService.getJobStatus( jobID );
    }

    public Object get() throws InterruptedException, ExecutionException
    {
        while ( executorService.getJobStatus( jobID ).equals( SlurmJobStatus.PENDING )
                || executorService.getJobStatus( jobID ).equals( SlurmJobStatus.RUNNING ) )
        {
            // wait
        }

        return null;
    }

    public Object get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
    {
        return null;
    }

}
