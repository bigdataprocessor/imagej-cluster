package de.embl.cba.cluster;

import de.embl.cba.cluster.job.SlurmJob;
import de.embl.cba.cluster.logger.Logger;
import de.embl.cba.cluster.ssh.SSHConnector;
import de.embl.cba.cluster.ssh.SSHConnectorSettings;

import java.io.File;
import java.io.IOException;
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
    private final String COULD_NOT_DETERMINE_JOB_STATUS = "Could not determine job getStatus";

    public static final String OUTPUT = ".out";
    public static final String ERROR = ".err";

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

        renameJobFileWithJobID();

        return createJobFuture();
    }

    private void renameJobFileWithJobID( )
    {
        String tmpJobPath = remoteJobDirectory + File.separator + currentJobTemporaryFileName;
        String finalJobPath = remoteJobDirectory + File.separator + currentJobID + ".job";

        // Path source = Paths.get( Utils.localMounting( remoteJobDirectory + File.separator + currentJobTemporaryFileName  ) );
        //    Files.move( source,  source.resolveSibling( Utils.localMounting( finalJobPath ) ) );

        sshConnector.rename( tmpJobPath, finalJobPath );

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

    private void prepareJobSubmission( )
    {
        setupRemoteJobDirectory();
        setupTemporaryJobFilename();

        try
        {
            createJobFileOnRemoteServer( slurmJob.getJobText( this ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace(); // TODO
        }

    }

    private void setupRemoteJobDirectory()
    {

        try
        {
            String directory = "/g/cba/cluster/" + sshConnector.userName();
            Logger.log( "Creating remote directory: " + directory );
            sshConnector.executeCommand( "mkdir " + directory );
            remoteJobDirectory = directory;
        }
        catch ( Exception e )
        {
            Logger.error( e.toString() );
        }
    }

    public String readJobOutput( long jobID ) throws IOException
    {
        return sshConnector.readRemoteTextFileUsingSFTP( getJobDirectory( jobID ), getJobOutFilename( jobID )  );
    }

    public String readJobError( long jobID ) throws IOException
    {
        return sshConnector.readRemoteTextFileUsingSFTP( getJobDirectory( jobID ), getJobErrFilename( jobID )  );
    }

    private void runJobOnRemoteServer()
    {
        try
        {
            ArrayList< String > response = sshConnector.executeCommand( SUBMIT_JOB_COMMAND + remoteJobDirectory + "/" + currentJobTemporaryFileName );

            if ( response.get( 0 ).contains( SUCCESSFUL_JOB_SUBMISSION_RESPONSE ) )
            {
                String tmp = response.get( 0 ).replace( SUCCESSFUL_JOB_SUBMISSION_RESPONSE, "" );
                currentJobID = Integer.parseInt( tmp.trim() );
            }
            else
            {
                Logger.error( "Job submission failed!" );
            }

        }
        catch ( Exception e )
        {
            // TODO
            e.printStackTrace();
        }
    }

    public String getJobStatus( long jobID )
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

    private void createJobFileOnRemoteServer( String jobText ) throws Exception
    {

        //remoteJobPath = remoteJobDirectory + File.separator + currentJobTemporaryFileName;
        //Utils.saveTextAsFile( jobText, Utils.localMounting( remoteJobPath ) );

        sshConnector.saveTextAsFileOnRemoteServerUsingSFTP( jobText, remoteJobDirectory, currentJobTemporaryFileName );

    }

    private void setupTemporaryJobFilename()
    {
        Random random = new Random();
        currentJobTemporaryFileName = ( random.nextLong() & Long.MAX_VALUE ) + ".job";
    }

    public String getJobDirectory( long jobID )
    {
        return remoteJobDirectory;
    }

    public String getCurrentJobOutPath()
    {
        return remoteJobDirectory + "/" + "%j" + OUTPUT;
    }

    public String getCurrentJobErrPath()
    {
        return remoteJobDirectory + "/" + "%j" + ERROR;
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
