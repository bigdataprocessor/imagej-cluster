package de.embl.cba.cluster;

import de.embl.cba.cluster.job.SlurmJob;
import de.embl.cba.cluster.ssh.SSHConnector;
import de.embl.cba.cluster.ssh.SSHConnectorSettings;
import net.imglib2.Localizable;

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
    private final String COULD_NOT_DETERMINE_JOB_STATUS = "Could not determine job status";

    private String remoteJobDirectory;
    private String remoteJobPath;
    private String currentJobFileName;

    private Map< Long, String > jobErrorPath;
    private Map< Long, String > jobOutputPath;

    public SlurmExecutorService( SSHConnectorSettings loginSettings )
    {
        sshConnector = new SSHConnector( loginSettings );
        jobErrorPath = new HashMap<>(  );
        jobOutputPath = new HashMap<>(  );
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

    public SlurmJobFuture submit( SlurmJob slurmJob ) throws IOException
    {

        prepareJobSubmission( slurmJob );

        Long jobID = runJobOnRemoteServer();

        registerJob( jobID );

        return createJobFuture( slurmJob, jobID );
    }

    private void registerJob( long jobID )
    {
        jobErrorPath.put( jobID, getCurrentJobErrorPath() );
        jobOutputPath.put( jobID, getCurrentJobOutputPath() );
    }

    private SlurmJobFuture createJobFuture( SlurmJob slurmJob, Long jobID )
    {
        if ( jobID != null )
        {
            SlurmJobFuture slurmJobFuture = new SlurmJobFuture( this, slurmJob, jobID );
            return slurmJobFuture;
        }
        else
        {
            return null;
        }
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

    private void prepareJobSubmission( SlurmJob job ) throws IOException
    {
        Logger.log( "Preparing job submission..." );
        setupRemoteJobDirectory();
        setupRemoteJobFilename();
        createJobFileOnRemoteServer( job.getJobText( this ) );
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
        return Utils.readTextFile( Utils.localMounting( jobOutputPath.get( jobID ) ));
    }

    public String readJobError( long jobID ) throws IOException
    {
        return Utils.readTextFile( Utils.localMounting( jobErrorPath.get( jobID ) ) );
    }

    private Long runJobOnRemoteServer()
    {
        Logger.log( "Submitting job..." );
        try
        {
            ArrayList< String > response = sshConnector.executeCommand( SUBMIT_JOB_COMMAND + remoteJobPath );

            if ( response.get( 0 ).contains( SUCCESSFUL_JOB_SUBMISSION_RESPONSE ) )
            {
                String tmp = response.get( 0 ).replace( SUCCESSFUL_JOB_SUBMISSION_RESPONSE, "" );
                long jobID = Integer.parseInt( tmp.trim() );
                Logger.log( "...done." );
                return jobID;
            }

        }
        catch ( Exception e )
        {
            //
        }

        return null;
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

        //sshConnector.saveTextAsFileOnRemoteServerUsingSFTP( slurmJobFuture.getJobText(), jobFileName, remoteJobDirectoryAsMountedRemotely );

        Utils.saveTextAsFile( jobText, currentJobFileName, Utils.localMounting( remoteJobDirectory ) );

        remoteJobPath = remoteJobDirectory + File.separator + currentJobFileName;

    }

    private void setupRemoteJobFilename()
    {
        currentJobFileName = Utils.timeStamp() + "-job.sh";
    }

    public String getRemoteJobDirectory()
    {
        return remoteJobDirectory;
    }


    public String getCurrentJobOutputPath()
    {
        return remoteJobDirectory + "/" + currentJobFileName + ".out" ;
    }

    public String getCurrentJobErrorPath()
    {
        return remoteJobDirectory + "/" + currentJobFileName + ".err" ;
    }

    public String getCurrentJobFileName()
    {
        return currentJobFileName;
    }


}
