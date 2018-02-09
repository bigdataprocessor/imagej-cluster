package de.embl.cba.cluster;

import de.embl.cba.cluster.job.JobScript;
import de.embl.cba.cluster.ssh.SSHConnector;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

// TODO: set remote tmp dir via some default mechanism
// TODO: option to submit job just as long remoteRootDirectory (easier than file)
// TODO: how to do the manageJobDependencies such that it is generic?
// TODO: implement logic of whether remote system is windows or linux

// https://www.rc.fas.harvard.edu/resources/documentation/convenient-slurm-commands/

public class SSHExecutorService implements ExecutorService
{
    // input
    private final SSHConnector sshConnector;
    private JobScript jobScript;

    public static final String SLURM_JOB = "Slurm";
    public static final String LINUX_JOB = "Linux";

    public static final String SLURM_JOB_SUBMISSION_COMMAND = "sbatch ";
    public static final String LINUX_JOB_SUBMISSION_COMMAND = "";

    private final String SLURM_JOB_STATUS_COMMAND = "sacct -j ";
    private final String SLURM_SUCCESSFUL_JOB_SUBMISSION_RESPONSE = "Submitted batch job ";

    private final String COULD_NOT_DETERMINE_JOB_STATUS = "Could not determine job status";

    public static final String OUTPUT = ".out";
    public static final String ERROR = ".err";
    public static final String STARTED = ".started";
    public static final String FINISHED = ".finished";
    public static final String JOB = ".job";

    private String jobDirectory;
    private long jobID;

    private String jobSubmissionType;

    // Commands
    private String submitJobCommand;
    private String pipeOutputCommand;
    private String makeRemoteDirectoryCommand;
    private String makeScriptExecutableCommand;
    private String createEmptyFileCommand;

    public SSHExecutorService( SSHConnector sshConnector,
                               String jobDirectory,
                               String jobSubmissionType )
    {
        this.sshConnector = sshConnector;

        this.jobDirectory = jobDirectory;
        this.jobSubmissionType = jobSubmissionType;

        makeRemoteDirectoryCommand = "mkdir ";
        makeScriptExecutableCommand = "chmod +x ";
        createEmptyFileCommand = "touch ";
    }

    public void shutdown()
    {

    }

    public List< Runnable > shutdownNow()
    {
        return null;
    }

    public boolean isShutdown()
    {
        return false;
    }

    public boolean isTerminated()
    {
        return false;
    }

    public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException
    {
        return false;
    }

    public < T > Future< T > submit( Callable< T > task )
    {
        return null;
    }

    public < T > Future< T > submit( Runnable task, T result )
    {
        return null;
    }

    public Future< ? > submit( Runnable runnable )
    {
        // ISSUE: I could not make the Runnable concept work, as I had no good idea
        // what the .run() method show do in a cluster context
        return null;
    }

    public synchronized JobFuture submit( JobScript jobScript )
    {
        this.jobScript = jobScript;

        ensureExistenceOfRemoteJobDirectory();

        setJobID();

        createRemoteJobFile();

        submitJob();

        return createJobFuture();
    }

    private JobFuture createJobFuture( )
    {
        JobFuture jobFuture = new JobFuture( this, jobID );

        return jobFuture;
    }

    public < T > List< Future< T > > invokeAll( Collection< ? extends Callable< T > > tasks ) throws InterruptedException
    {
        return null;
    }

    public < T > List< Future< T > > invokeAll( Collection< ? extends Callable< T > > tasks, long timeout, TimeUnit unit ) throws InterruptedException
    {
        return null;
    }

    public < T > T invokeAny( Collection< ? extends Callable< T > > tasks ) throws InterruptedException, ExecutionException
    {
        return null;
    }

    public < T > T invokeAny( Collection< ? extends Callable< T > > tasks, long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
    {
        return null;
    }

    public void execute( Runnable runnable )
    {
    }

    private void ensureExistenceOfRemoteJobDirectory()
    {
        try
        {
            Utils.logger.info( "Creating remote directory: " + jobDirectory );
            sshConnector.executeCommand( makeRemoteDirectoryCommand + jobDirectory );
        }
        catch ( Exception e )
        {
            Utils.logger.error( e.toString() );
        }
    }

    public String getJobOutput( long jobID )
    {
        return sshConnector.readRemoteTextFileUsingSFTP( jobDirectory, getJobOutFilename( jobID )  );
    }

    public String getJobError( long jobID )
        {
        return sshConnector.readRemoteTextFileUsingSFTP( jobDirectory, getJobErrFilename( jobID )  );
    }

    private HashMap< String, ArrayList< String > > submitJob()
    {
        HashMap< String, ArrayList< String > > responses = sshConnector.executeCommand( getJobSubmissionCommand() );
        return responses;
    }

    private String getJobSubmissionCommand()
    {
        String jobSubmissionCommand = "";

        if ( jobSubmissionType.equals( SLURM_JOB ) )
        {
            jobSubmissionCommand = SLURM_JOB_SUBMISSION_COMMAND;
        }
        else if ( jobSubmissionType.equals( LINUX_JOB ) )
        {
            jobSubmissionCommand = LINUX_JOB_SUBMISSION_COMMAND;
        }

        jobSubmissionCommand += jobDirectory + sshConnector.remoteFileSeparator() + getJobFilename( jobID );

        if ( jobSubmissionType.equals( SLURM_JOB ) )
        {
            //
        }
        else if ( jobSubmissionType.equals( LINUX_JOB ) )
        {
            jobSubmissionCommand += " > " + getCurrentJobOutPath() + " &";
        }

        return jobSubmissionCommand;
    }


    public boolean isDone( long jobID )
    {
        return sshConnector.fileExists( jobDirectory + sshConnector.remoteFileSeparator() + getJobFinishedFilename( jobID ) );
    }

    public boolean isStarted( long jobID )
    {
        return sshConnector.fileExists( jobDirectory + sshConnector.remoteFileSeparator() + getJobStartedFilename( jobID ) );
    }

    private String getJobFinishedFilename( long jobID )
    {
        return jobID + FINISHED;
    }

    private String getJobStartedFilename( long jobID )
    {
        return jobID + STARTED;
    }


    private void createRemoteJobFile( )
    {
        String jobText = jobScript.getJobText( this );
        sshConnector.saveTextAsFileOnRemoteServerUsingSFTP( jobText, jobDirectory, getJobFilename( jobID ) );
        //sshConnector.executeCommand( makeScriptExecutableCommand + jobDirectory + sshConnector.remoteFileSeparator() + getJobFilename( jobID ) );
    }

    private void setJobID()
    {
        Random random = new Random();
        jobID = random.nextLong() & Long.MAX_VALUE;
    }

    public String getJobDirectory()
    {
        return jobDirectory;
    }

    public String getCurrentJobOutPath()
    {
        return getJobOutPath( jobID );
    }

    public String getCurrentJobErrPath()
    {
        return getJobErrPath( jobID );
    }

    public String getJobOutPath( long jobID )
    {
        return jobDirectory + sshConnector.remoteFileSeparator() + getJobOutFilename( jobID  );
    }

    public String getJobErrPath( long jobID )
    {
        return jobDirectory + sshConnector.remoteFileSeparator() + getJobErrFilename( jobID  );
    }


    public String getJobStartedCommand()
    {
        String command = createEmptyFileCommand + jobDirectory + sshConnector.remoteFileSeparator() + getJobStartedFilename( jobID );

        return command;
    }

    public String getJobFinishedCommand()
    {
        String command = createEmptyFileCommand + jobDirectory + sshConnector.remoteFileSeparator() + getJobFinishedFilename( jobID );

        return command;
    }

    public String getJobOutFilename( long jobID  )
    {
        return jobID + OUTPUT ;
    }

    public String getJobErrFilename( long jobID  )
    {
        return jobID + ERROR ;
    }

    private String getJobFilename( long jobID )
    {
        return jobID + JOB;
    }


}
