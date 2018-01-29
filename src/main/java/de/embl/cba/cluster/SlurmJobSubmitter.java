package de.embl.cba.cluster;

import de.embl.cba.cluster.job.ImageJCommandSlurmJob;
import de.embl.cba.cluster.job.SlurmJob;
import de.embl.cba.cluster.ssh.SSHConnectorSettings;

import java.io.IOException;
import java.util.ArrayList;

public abstract class SlurmJobSubmitter
{

    public static SlurmJobFuture submit( ArrayList< String >  commands, String username, String password )
    {

        ImageJCommandSlurmJob imageJCommandSlurmJob = new ImageJCommandSlurmJob( commands );

        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings( username, password, SSHConnectorSettings.EMBL_SLURM_HOST );

        SlurmExecutorService executorService = new SlurmExecutorService( sshConnectorSettings );

        SlurmJobFuture future = submitJob( imageJCommandSlurmJob, executorService );

        return future;

    }

    private static SlurmJobFuture submitJob( SlurmJob imageJCommandSlurmJob, SlurmExecutorService executorService )
    {
        SlurmJobFuture future = null;

        try
        {
            future = executorService.submit( imageJCommandSlurmJob );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        return future;
    }

}
