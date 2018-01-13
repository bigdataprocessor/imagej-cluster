package de.embl.cba.cluster;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class SlurmExecutorService implements ExecutorService
{
    private final SSHConnector sshConnector;
    private final String remoteJobDirectory;

    public SlurmExecutorService( SSHConnectorSettings loginSettings, String remoteJobDirectory )
    {
        sshConnector = new SSHConnector( loginSettings );
        // TODO: set remote tmp dir via some mechanism
        this.remoteJobDirectory = remoteJobDirectory;
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
        submitJob( slurmJobFuture );
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

    private long submitJob( SlurmJobFuture slurmJobFuture )
    {

        Date now = new Date();
        String jobFileName = sshConnector.userName() + "--" + now.toString() + ".sh";
        sshConnector.saveTextAsFileOnRemoteServer( slurmJobFuture.getJobText(), jobFileName,  );
        long jobID = 0;

        // submit job on cluster and get jobID

        return  jobID;
    }

    public String checkJobStatus( long jobID )
    {
        return null;
    }

}
