package de.embl.cba.cluster;

public class ClusterRunnable implements Runnable
{
    private SlurmExecutorService slurmExecutorService;
    private Runnable runnable;
    public long jobID;

    public ClusterRunnable( Runnable runnable, SlurmExecutorService slurmExecutorService )
    {
        this.runnable = runnable;
        this.slurmExecutorService = slurmExecutorService;

    }

    public void run()
    {
        jobID = slurmExecutorService.submitJob( jobText() );
    }

    private String jobText()
    {
        runnable.toString();

        // create jobText based on runnable, but how?
        return null;
    }

}
