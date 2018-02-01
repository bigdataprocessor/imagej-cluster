package de.embl.cba.cluster;

import de.embl.cba.cluster.job.Job;
import de.embl.cba.cluster.logger.Logger;
import de.embl.cba.cluster.ssh.SSHConnector;
import de.embl.cba.cluster.ssh.SSHConnectorSettings;

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
    private Job job;

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


    private String remoteJobDirectory;
    private long jobID;
    private String jobFileName;
    private String successfulJobSubmissionResponse;

    // Commands
    private String jobSubmissionCommand;
    private String makeRemoteDirectoryCommand;
    private String makeScriptExecutableCommand;
    private String createEmptyFileCommand;

    public SSHExecutorService( SSHConnectorSettings loginSettings,
                               String remoteJobDirectory,
                               String jobSubmissionCommand)
    {
        this.sshConnector = new SSHConnector( loginSettings );
        this.remoteJobDirectory = remoteJobDirectory;

        makeRemoteDirectoryCommand = "mkdir ";
        makeScriptExecutableCommand = "chmod +x ";

        jobSubmissionCommand = jobSubmissionCommand;
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

    public synchronized JobFuture submit( Job job ) throws IOException
    {
        this.job = job;

        ensureExistenceOfRemoteJobDirectory();

        setJobID();

        createRemoteJobFile();

        runJob();

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
            Logger.log( "Creating remote directory: " + remoteJobDirectory );
            sshConnector.executeCommand( makeRemoteDirectoryCommand + remoteJobDirectory );
        }
        catch ( Exception e )
        {
            Logger.error( e.toString() );
        }
    }

    public String getJobOutput( long jobID ) throws IOException
    {
        return sshConnector.readRemoteTextFileUsingSFTP( getJobDirectory( jobID ), getJobOutFilename( jobID )  );
    }

    public String getJobError( long jobID ) throws IOException
    {
        return sshConnector.readRemoteTextFileUsingSFTP( getJobDirectory( jobID ), getJobErrFilename( jobID )  );
    }

    private ArrayList< String > runJob()
    {
        ArrayList< String > responses = sshConnector.executeCommand( jobSubmissionCommand + remoteJobDirectory + "/" + jobFileName );
        return responses;
    }

    public String getJobStatus( long jobID )
    {
        String cmd = SLURM_JOB_STATUS_COMMAND + jobID + " --format=State";

        try
        {
            ArrayList< String > responses = sshConnector.executeCommand( cmd );
            String lastResponse = responses.get( responses.size() - 2 );
            return lastResponse.trim();
        }
        catch ( Exception e )
        {
            return COULD_NOT_DETERMINE_JOB_STATUS;
        }

    }

    private void createRemoteJobFile( )
    {
        String jobText = job.getJobText( this );
        sshConnector.saveTextAsFileOnRemoteServerUsingSFTP( jobText, remoteJobDirectory, jobFileName );
        sshConnector.executeCommand( makeScriptExecutableCommand +  remoteJobDirectory + sshConnector.remoteFileSeparator() + jobFileName );
    }

    private void setJobID()
    {
        Random random = new Random();
        jobID = random.nextLong() & Long.MAX_VALUE;
        jobFileName = jobID + JOB;
    }

    public String getJobDirectory( long jobID )
    {
        return remoteJobDirectory;
    }

    public String getCurrentJobOutPath()
    {
        return remoteJobDirectory + "/" + jobID + OUTPUT;
    }

    public String getReportJobStartedCommand()
    {
        String command = createEmptyFileCommand + jobID + STARTED;

        return command;
    }

    public String getReportJobFinishedCommand()
    {
        String command = createEmptyFileCommand + jobID + FINISHED;

        return command;
    }

    public String getCurrentJobErrPath()
    {
        return remoteJobDirectory + "/" + jobID + ERROR;
    }

    public String getJobOutPath( long jobID  )
    {
        return remoteJobDirectory + "/" + jobID + OUTPUT ;
    }

    public String getJobOutFilename( long jobID  )
    {
        return jobID + OUTPUT ;
    }

    public String getJobErrFilename( long jobID  )
    {
        return jobID + ERROR ;
    }

    public String getJobErrPath( long jobID  )
    {
        return remoteJobDirectory + "/" + jobID + OUTPUT ;
    }


}
