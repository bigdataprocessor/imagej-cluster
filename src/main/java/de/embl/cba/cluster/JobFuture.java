package de.embl.cba.cluster;

import de.embl.cba.cluster.job.JobScript;
import de.embl.cba.cluster.ssh.SSHConnector;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JobFuture implements Future
{
    SSHExecutorService executorService;
    String jobID;
    JobScript jobScript;

    public static final String STD_OUT = "StdOut";
    public static final String STD_ERR = "StdErr";
    public static final String NO_ERROR = "No error";

    public static final String XVFB_ERROR_01 = "xvfb-run";
    public static final String XVFB_ERROR_02 = "SocketCreateListener";
    public static final String SLURM_TIME_LIMIT_ERROR = "timeLimit";
    public static final String SLURM_STEP_ERROR = "slurmstepd";
    public static final String UNKNOWN_ERROR = "unkown";

    public static final int MAX_NUM_SUBMISSIONS = 5;

    private int numReSubmissions;

    private String status;

    public static final String SUBMITTED = "submitted";
    public static final String RESUBMITTED = "resubmitted";
    public static final String FAILED = "failed";
    public static final String RUNNING = "running";
    public static final String ERROR = "error";
    public static final String FINISHED = "finished";

    boolean finished;
    boolean failed;

    public JobFuture( SSHExecutorService executorService, String jobID, JobScript jobScript )
    {
        this.executorService = executorService;
        this.jobID = jobID;
        this.jobScript = jobScript;
        numReSubmissions = 0;
        status = SUBMITTED;
        finished = false;
        failed = false;
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

    public String getJobID()
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

    public String getStatus()
    {
        if ( status.endsWith( FINISHED ) )
        {
            return status;
        }

        if ( status.endsWith( SUBMITTED ) )
        {
            if ( isStarted() )
            {
                status += "-running";
            }
        }

        if ( status.endsWith( RUNNING ) )
        {
            String error = examineError();
            if ( ! error.equals( NO_ERROR ) )
            {
                status += "-" + error + "_" + ERROR;
            }
        }

        if ( status.endsWith( ERROR ) )
        {
            if ( numReSubmissions < MAX_NUM_SUBMISSIONS )
            {
                resubmit();
                status += "-" + RESUBMITTED;
            }
            else
            {
                status += "-" + FAILED;
            }

            return status;
        }

        if ( isDone() )
        {
            status += "-" + FINISHED;
        }

        return ( status );

    }

    public boolean hasFailed()
    {
        if ( failed )
        {
            return true;
        }
        else
        {
            failed = status.endsWith( FAILED );
            return failed;
        }
    }

    public int getNumReSubmissions()
    {
        return numReSubmissions;
    }

    public boolean isDone()
    {
        if ( finished )
        {
            return true;
        }
        else
        {
            finished = executorService.isFinished( jobID );
            return finished;
        }
    }


    public String examineError()
    {
        String err = executorService.getJobError( jobID );

        if ( err.contains( XVFB_ERROR_01 ) )
        {
            return XVFB_ERROR_01;
        }
        else if ( err.contains( XVFB_ERROR_02 ) )
        {
            return XVFB_ERROR_02;
        }
        else if ( err.contains( "DUE TO TIME LIMIT" ) )
        {
            return SLURM_TIME_LIMIT_ERROR;
        }
        else if ( err.contains( SLURM_STEP_ERROR ) )
        {
            return SLURM_STEP_ERROR;
        }
        else if ( err.equals( SSHConnector.IO_EXCEPTION ) )
        {
            return NO_ERROR; // error file could not be read
        }
        else if ( err.equals( SSHConnector.SFTP_EXCEPTION ) )
        {
            return NO_ERROR; // error file could not be read
        }
        else if ( err.length() > 10 )
        {
            return UNKNOWN_ERROR;
        }

        return NO_ERROR;
    }

    public void resubmit()
    {
        renameBasedOnSubmissionNumber( executorService.getJobStartedPath( jobID ) );
        renameBasedOnSubmissionNumber( executorService.getJobFinishedPath( jobID ) );
        renameBasedOnSubmissionNumber( executorService.getJobOutPath( jobID ) );
        renameBasedOnSubmissionNumber( executorService.getJobErrPath( jobID ) );
        renameBasedOnSubmissionNumber( executorService.getJobXvfbErrPath( jobID ) );

        executorService.submit( jobScript, jobID );

        numReSubmissions++;
    }

    private void renameBasedOnSubmissionNumber( String filePath )
    {
        executorService.getSshConnector().rename( filePath, filePath + "_" + numReSubmissions );
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

        while ( ! executorService.isFinished( jobID ) )
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
