package de.embl.cba.cluster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public SlurmJobFuture submit( ImageJGroovyScriptJob imageJGroovyScriptJob ) throws IOException
    {

        prepareJobSubmission( imageJGroovyScriptJob );

        Long jobID = runJobOnRemoteServer();

        return createJobFuture( imageJGroovyScriptJob, jobID );

    }

    private SlurmJobFuture createJobFuture( ImageJGroovyScriptJob imageJGroovyScriptJob, Long jobID )
    {
        if ( jobID != null )
        {
            SlurmJobFuture slurmJobFuture = new SlurmJobFuture( this, imageJGroovyScriptJob, jobID );
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

    private void prepareJobSubmission( ImageJGroovyScriptJob job ) throws IOException
    {
        setupRemoteJobDirectory();
        setupRemoteJobFilename();

        job.manageDependencies( this );
        job.setJobFilename( currentJobFileName );

        createJobFileOnRemoteServer( job.jobText() );

    }


    private void setupRemoteJobDirectory()
    {
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

    }

    private Long runJobOnRemoteServer()
    {
        try
        {
            ArrayList< String > response = sshConnector.executeCommand( SUBMIT_JOB_COMMAND + remoteJobPath );

            if ( response.get( 0 ).contains( SUCCESSFUL_JOB_SUBMISSION_RESPONSE ) )
            {
                String tmp = response.get( 0 ).replace( SUCCESSFUL_JOB_SUBMISSION_RESPONSE, "" );
                long jobID = Integer.parseInt( tmp.trim() );
                return jobID;
            }

        }
        catch ( Exception e )
        {
            //
        }

        return null;
    }

    public String jobStatus( long jobID )
    {
        String cmd = JOB_STATUS_COMMAND + jobID + " --format=State";

        try
        {
            ArrayList< String > responses = sshConnector.executeCommand( cmd );
            String lastResponse = responses.get( responses.size() - 2 );
            return lastResponse;
        }
        catch ( Exception e )
        {
            return COULD_NOT_DETERMINE_JOB_STATUS;
        }

    }

    private void createJobFileOnRemoteServer( String jobText )
    {

        //sshConnector.saveTextAsFileOnRemoteServerUsingSFTP( slurmJobFuture.jobText(), jobFileName, remoteJobDirectoryAsMountedRemotely );

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


}
