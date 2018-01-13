package de.embl.cba.cluster;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

// TODO: set remote tmp dir via some default mechanism
// TODO: option to submit job just as long string (easier than file)


public class SlurmExecutorService implements ExecutorService
{
    private final SSHConnector sshConnector;
    private final String remoteJobDirectoryAsMountedLocally;
    private final String remoteJobDirectoryAsMountedRemotely;

    private final String SUBMIT_JOB_COMMAND = "sbatch ";
    private final String SUCCESSFUL_JOB_SUBMISSION_RESPONSE = "Submitted batch job ";

    public SlurmExecutorService( SSHConnectorSettings loginSettings,
                                 String remoteJobDirectoryAsMountedLocally,
                                 String remoteJobDirectoryAsMountedRemotely)
    {
        sshConnector = new SSHConnector( loginSettings );
        this.remoteJobDirectoryAsMountedLocally = remoteJobDirectoryAsMountedLocally;
        this.remoteJobDirectoryAsMountedRemotely = remoteJobDirectoryAsMountedRemotely;
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

    public Future< ? > submit( SlurmJobScript jobScript )
    {
        SlurmJobFuture slurmJobFuture = new SlurmJobFuture( jobScript );

        try
        {
            submitJob( slurmJobFuture );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

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

    private long submitJob( SlurmJobFuture slurmJobFuture ) throws Exception
    {
        String remoteJobPath = putJobFileOntoRemoteServer( slurmJobFuture );
        String outAndErrSystemResponse = sshConnector.executeCommand( SUBMIT_JOB_COMMAND + remoteJobPath );

        String response = outAndErrSystemResponse;

        if ( ! response.contains( SUCCESSFUL_JOB_SUBMISSION_RESPONSE ) )
        {
            // Throw some error
            return -1;
        }
        else
        {
            String tmp = response.replace( SUCCESSFUL_JOB_SUBMISSION_RESPONSE, "" ).trim();
            long jobID = Integer.parseInt( tmp );
        }

        return jobID;
    }

    private String putJobFileOntoRemoteServer( SlurmJobFuture slurmJobFuture )
    {
        String jobFileName = sshConnector.userName() + "--" + Utils.timeStamp() + ".sh";

        //sshConnector.saveTextAsFileOnRemoteServerUsingSFTP( slurmJobFuture.getJobText(), jobFileName, remoteJobDirectoryAsMountedRemotely );

        Utils.saveTextAsFile( slurmJobFuture.getJobText(), jobFileName, remoteJobDirectoryAsMountedLocally );

        return remoteJobDirectoryAsMountedRemotely + File.separator + jobFileName;
    }

    public String checkJobStatus( long jobID )
    {
        return null;
    }

}
