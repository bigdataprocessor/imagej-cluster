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
    public static final String SLURM_TIME_LIMIT_ERROR = "DUE TO TIME LIMIT";
    public static final String SLURM_STEP_ERROR = "slurmstepd";
    public static final String JVM_WARNING = "VM warning";

    public static final String UNKNOWN_ERROR = "unkown";

    public static final int MAX_NUM_SUBMISSIONS = 5;

    private int numReSubmissions;

    private String statusHistory;
    private String currentStatus;

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
        statusHistory = SUBMITTED;
        finished = false;
        failed = false;
        currentStatus = SUBMITTED;
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


    public void refreshStatus()
    {

        if ( currentStatus.equals( FINISHED ) )
        {
            return;
        }

        if ( currentStatus.equals( SUBMITTED ) )
        {
            if ( isStarted() )
            {
                currentStatus = RUNNING;
                statusHistory += "-" + RUNNING;
            }
        }

        if ( currentStatus.equals( RUNNING ) )
        {
            String error = checkForErrors();

            if ( error.equals( NO_ERROR ) )
            {
                if ( executorService.isFinished( jobID ) )
                {
                    currentStatus = FINISHED;
                    statusHistory += "-" + FINISHED;
                }
            }
            else
            {
                currentStatus = ERROR;
                statusHistory += "-" + error + "_" + ERROR;
            }

        }

    }

    public String getStatusHistory()
    {
        return statusHistory;
    }

    public String getCurrentStatus()
    {
        return currentStatus;
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
            return false;
        }
    }


    public String checkForErrors()
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
        else if ( err.contains( SLURM_TIME_LIMIT_ERROR ) )
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
        else if ( err.contains( JVM_WARNING ) )
        {
            return NO_ERROR;
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

        statusHistory += "-" + RESUBMITTED;

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
