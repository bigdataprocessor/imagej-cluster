package de.embl.cba.cluster.plugins;

import de.embl.cba.cluster.SlurmExecutorService;
import de.embl.cba.cluster.SlurmJobFuture;
import de.embl.cba.cluster.SlurmJobStatus;
import de.embl.cba.cluster.job.ImageJCommandSlurmJob;
import de.embl.cba.cluster.job.SlurmJob;
import de.embl.cba.cluster.ssh.SSHConnectorSettings;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.TextWidget;

import java.io.IOException;

@Plugin(type = Command.class, menuPath = "Plugins>EMBL-CBA>Cluster Command Runner" )
public class SlurmCommandRunnerPlugin implements Command
{
    @Parameter(label = "User name" )
    private String username;

    @Parameter(label = "Password", style = TextWidget.PASSWORD_STYLE )
    private String password;

    @Parameter(label = "Command and parameters" )
    private String commandAndParameters;

    public void run()
    {

        ImageJCommandSlurmJob imageJCommandSlurmJob = new ImageJCommandSlurmJob( commandAndParameters );

        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings( username, password, SSHConnectorSettings.EMBL_SLURM_HOST );

        SlurmExecutorService executorService = new SlurmExecutorService( sshConnectorSettings );

        SlurmJobFuture future = submitJob( imageJCommandSlurmJob, executorService );

        
        try
        {
            SlurmJobStatus.monitorJobStatusAndShowOutAndErrWhenDone( future );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

    }

    private SlurmJobFuture submitJob( SlurmJob imageJCommandSlurmJob, SlurmExecutorService executorService )
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
