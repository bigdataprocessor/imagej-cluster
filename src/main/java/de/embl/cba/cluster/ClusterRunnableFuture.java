package de.embl.cba.cluster;

import java.util.concurrent.*;

public class ClusterRunnableFuture implements RunnableFuture
{
    Runnable runnable;

    public ClusterRunnableFuture( Runnable runnable )
    {
        this.runnable = runnable;
    }

    public boolean cancel( boolean mayInterruptIfRunning )
    {
        return false;
    }

    public boolean isCancelled()
    {
        return false;
    }

    public boolean isDone()
    {
        return false;
    }

    public Object get() throws InterruptedException, ExecutionException
    {
        return null;
    }

    public Object get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
    {
        return null;
    }

    public void run()
    {
        runnable.run();
    }
}
