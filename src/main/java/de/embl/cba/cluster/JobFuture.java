package de.embl.cba.cluster;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JobFuture implements Future
{

    SSHExecutorService executorService;
    long jobID;

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
            error = executorService.getJobError( jobID );
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
            output = executorService.getJobOutput( jobID );
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
