package de.embl.cba.cluster;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ClusterExecutorService implements ExecutorService
{
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

    public Future< ? > submit( Runnable task )
    {
        if (task == null) throw new NullPointerException();
        ClusterRunnableFuture clusterRunnableFuture = newClusterRunnableFuture( task );
        execute( clusterRunnableFuture );
        return clusterRunnableFuture;
    }

    protected ClusterRunnableFuture newClusterRunnableFuture( Runnable runnable )
    {
            return new ClusterRunnableFuture( runnable );
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

    public void execute( Runnable command )
    {
        // Here the command is submitted to the cluster by executing the run() method of command, which is a ClusterRunnableFuture

    }
}
