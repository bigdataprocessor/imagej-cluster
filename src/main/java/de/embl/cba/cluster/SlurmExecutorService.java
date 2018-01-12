package de.embl.cba.cluster;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class SlurmExecutorService implements ExecutorService
{
    SlurmLoginSettings loginSettings;

    public SlurmExecutorService( SlurmLoginSettings loginSettings )
    {
        this.loginSettings = loginSettings;
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
        ClusterRunnable clusterRunnable = new ClusterRunnable( runnable, this );
        ClusterRunnableFuture clusterRunnableFuture = new ClusterRunnableFuture( clusterRunnable, this );
        execute( clusterRunnableFuture );
        return clusterRunnableFuture;
    }

    public Future< ? > submit( SlurmJobScript jobScript )
    {
        SlurmJobFuture slurmJobFuture = new SlurmJobFuture( jobScript );
        // executableCommands future
        // return future

        return clusterRunnableFuture;

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

    private long submitJob( String jobText )
    {
        long jobID = 0;

        // submit job on cluster and get jobID

        return  jobID;
    }

    public String checkJobStatus( long jobID )
    {
        return null;
    }

}
