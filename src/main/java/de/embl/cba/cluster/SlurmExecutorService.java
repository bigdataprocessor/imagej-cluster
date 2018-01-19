package de.embl.cba.cluster;

import de.embl.cba.cluster.job.SlurmJob;
import de.embl.cba.cluster.ssh.SSHConnector;
import de.embl.cba.cluster.ssh.SSHConnectorSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

// TODO: set remote tmp dir via some default mechanism
// TODO: option to submit job just as long string (easier than file)
// TODO: how to do the manageJobDependencies such that it is generic?

// https://www.rc.fas.harvard.edu/resources/documentation/convenient-slurm-commands/

public class SlurmExecutorService implements ExecutorService
{
    private final SSHConnector sshConnector;

    private final String SUBMIT_JOB_COMMAND = "sbatch ";
    private final String JOB_STATUS_COMMAND = "sacct -j ";

    private final String SUCCESSFUL_JOB_SUBMISSION_RESPONSE = "Submitted batch job ";
    private final String COULD_NOT_DETERMINE_JOB_STATUS = "Could not determine job status";

    private String remoteJobDirectory;
    private String remoteJobPath;
    private long currentJobID;
    private String currentJobTemporaryFileName;
    private SlurmJob slurmJob;

    public SlurmExecutorService( SSHConnectorSettings loginSettings )
    {
        sshConnector = new SSHConnector( loginSettings );
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

    public synchronized SlurmJobFuture submit( SlurmJob slurmJob ) throws IOException
    {

        this.slurmJob = slurmJob;

        prepareJobSubmission();

        runJobOnRemoteServer();

        changeJobFilenameToJobID();

        return createJobFuture();
    }

    private void changeJobFilenameToJobID( ) throws IOException
    {
        String finalJobPath = remoteJobDirectory + File.separator + currentJobID + ".job";
        Path source = Paths.get( Utils.localMounting( remoteJobPath ) );
        Files.move( source,  source.resolveSibling( finalJobPath ) );
    }

    private SlurmJobFuture createJobFuture( )
    {
        SlurmJobFuture slurmJobFuture = new SlurmJobFuture( this, slurmJob, currentJobID );
        return slurmJobFuture;
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

    private boolean isRemoteServerConnectionWorking()
    {
        // TODO implement within SSHConnector
        //command = "touch /g/cba/cluster/"+Utils.timeStamp();
        //command = "echo 'hello world'; sbatch echo 'hello world';";

        return true;
    }

    private void prepareJobSubmission( ) throws IOException
    {
        Logger.log( "Preparing job submission..." );
        setupRemoteJobDirectory();
        setupTemporaryJobFilename();
        createJobFileOnRemoteServer( slurmJob.getJobText( this ) );
        Logger.done();
    }

    private void setupRemoteJobDirectory()
    {
        Logger.log( "Creating remote job directory via SSH..." );
        try
        {
            String directory = "/g/cba/cluster/" + sshConnector.userName();
            sshConnector.executeCommand( "mkdir " + directory );
            remoteJobDirectory = directory;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        Logger.done();

    }

    public String readJobOutput( long jobID ) throws IOException
    {
        return Utils.readTextFile( Utils.localMounting( getJobOutPath( jobID ) ));
    }

    public String readJobError( long jobID ) throws IOException
    {
        return Utils.readTextFile( Utils.localMounting( getJobErrPath( jobID ) ) );
    }

    private void runJobOnRemoteServer()
    {
        Logger.log( "Submitting job..." );
        try
        {
            ArrayList< String > response = sshConnector.executeCommand( SUBMIT_JOB_COMMAND + remoteJobPath );

            if ( response.get( 0 ).contains( SUCCESSFUL_JOB_SUBMISSION_RESPONSE ) )
            {
                String tmp = response.get( 0 ).replace( SUCCESSFUL_JOB_SUBMISSION_RESPONSE, "" );
                currentJobID = Integer.parseInt( tmp.trim() );
                Logger.log( "...done." );
            }
            else
            {
                // TODO
            }

        }
        catch ( Exception e )
        {
            // TODO
            e.printStackTrace();
        }
    }

    public String checkJobStatus( long jobID )
    {
        String cmd = JOB_STATUS_COMMAND + jobID + " --format=State";

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

    private void createJobFileOnRemoteServer( String jobText )
    {

        remoteJobPath = remoteJobDirectory + File.separator + currentJobTemporaryFileName;

        Utils.saveTextAsFile( jobText, Utils.localMounting( remoteJobPath ) );

        //TODO: sshConnector.saveTextAsFileOnRemoteServerUsingSFTP( slurmJobFuture.getJobText(), jobFileName, remoteJobDirectoryAsMountedRemotely );

    }

    private void setupTemporaryJobFilename()
    {
        Random random = new Random();
        String number = "" + random.nextLong();
        String date = new Date().toString();

        currentJobTemporaryFileName = date + "--" + number + ".job";
    }

    public String getRemoteJobDirectory()
    {
        return remoteJobDirectory;
    }

    public static final String OUTPUT = ".out";
    public static final String ERROR = ".err";

    public String getCurrentJobOutPath()
    {
        return remoteJobDirectory + "/" + currentJobID + OUTPUT;
    }

    public String getCurrentJobErrPath()
    {
        return remoteJobDirectory + "/" + currentJobID + ERROR;
    }

    public String getJobOutPath( long jobID  )
    {
        return remoteJobDirectory + "/" + jobID + OUTPUT ;
    }

    public String getJobErrPath( long jobID  )
    {
        return remoteJobDirectory + "/" + jobID + OUTPUT ;
    }


}
