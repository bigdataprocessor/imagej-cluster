package de.embl.cba.cluster;

import de.embl.cba.cluster.job.SlurmJob;
import de.embl.cba.cluster.job.Job;
import de.embl.cba.cluster.ssh.SSHConnectorSettings;

import java.io.IOException;
import java.util.ArrayList;

public class ImageJJobSubmitter
{
    public static final String REMOTE_EMBL_SLURM_SYSTEM = "EMBL Slurm Cluster";

    public static final String IMAGEJ_EXECTUABLE_ALMF_CLUSTER_XVFB = "xvfb-run -a /g/almf/software/Fiji.app/ImageJ-linux64 --run";
    public static final String IMAGEJ_EXECTUABLE_ALMF_CLUSTER_HEADLESS = "/g/almf/software/Fiji.app/ImageJ-linux64 --ij2 --headless --run";
    public static final String CBA_CLUSTER_IMAGEJ = "xvfb-run -a /g/cba/software/Fiji.app/ImageJ-linux64 --ij2 --run";

    private String remoteSystem;
    private String remoteImageJExectuable;

    public ImageJJobSubmitter( String remoteSystem, String remoteImageJExectuable )
    {
        this.remoteSystem = remoteSystem;
        this.remoteImageJExectuable = remoteImageJExectuable;
    }


    public static JobFuture submit( ArrayList< String > commands, String username, String password, String remoteJobDirectory, String remoteSystem )
    {
        if ( remoteSystem.equals( REMOTE_EMBL_SLURM_SYSTEM ) )
        {
            commands.add( 0 , "module load Java" );
            commands.add( 0 , "module load X11" );
        }

        String jobSubmissionCommand = SSHExecutorService.SLURM_JOB_SUBMISSION_COMMAND;

        SlurmJob imageJCommandSlurmJob = new SlurmJob( commands );

        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings( username, password, SSHConnectorSettings.EMBL_SLURM_HOST );

        SSHExecutorService executorService = new SSHExecutorService( sshConnectorSettings, remoteJobDirectory, jobSubmissionCommand );

        JobFuture future = submitJob( imageJCommandSlurmJob, executorService );

        return future;

    }

    private static JobFuture submitJob( Job imageJCommandJob, SSHExecutorService executorService )
    {
        JobFuture future = null;

        try
        {
            future = executorService.submit( imageJCommandJob );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        return future;
    }

}
