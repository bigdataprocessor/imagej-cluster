package de.embl.cba.cluster;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

// TODO: set remote tmp dir via some default mechanism
// TODO: option to submit job just as long string (easier than file)


public class SlurmExecutorService implements ExecutorService
{
    private final SSHConnector sshConnector;
    private final String remoteJobDirectory;
    private String SUBMIT_SLURM_JOB_COMMAND = "sbatch ";

    public SlurmExecutorService( SSHConnectorSettings loginSettings, String remoteJobDirectory )
    {
        sshConnector = new SSHConnector( loginSettings );
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

    private long submitJob( SlurmJobFuture slurmJobFuture ) throws Exception
    {

        String jobFileName = sshConnector.userName() + "--" + timeStamp() + ".sh";
        //sshConnector.saveTextAsFileOnRemoteServerUsingSFTP( slurmJobFuture.getJobText(), jobFileName, remoteJobDirectory );
        sshConnector.saveTextAsFile( slurmJobFuture.getJobText(), jobFileName, remoteJobDirectory );

        String remoteJobPath = remoteJobDirectory + File.separator + jobFileName;
        sshConnector.executeCommand( SUBMIT_SLURM_JOB_COMMAND + remoteJobPath );

        // get long jobID = 0;

        return 0;
    }

    private String timeStamp()
    {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        return timeStamp;
    }

    public String checkJobStatus( long jobID )
    {
        return null;
    }

}
