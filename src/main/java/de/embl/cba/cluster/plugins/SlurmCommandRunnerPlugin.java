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
import java.util.ArrayList;

@Plugin(type = Command.class, menuPath = "Plugins>EMBL>Cluster Command Runner" )
public class SlurmCommandRunnerPlugin implements Command
{
    @Parameter(label = "User text" )
    private String username;

    @Parameter(label = "Password", style = TextWidget.PASSWORD_STYLE )
    private String password;

    @Parameter(label = "Command 1" )
    private String command1;

    @Parameter(label = "Command 2" )
    private String command2;

    @Parameter(label = "Command 3" )
    private String command3;


    public void run()
    {

        ArrayList< String > commands = new ArrayList<>(  );
        commands.add( command1 );
        commands.add( command2 );
        commands.add( command3 );

        ImageJCommandSlurmJob imageJCommandSlurmJob = new ImageJCommandSlurmJob( commands );

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
