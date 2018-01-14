import de.embl.cba.cluster.*;

import java.util.ArrayList;

public class GroovyIJ2PluginHelloWorld
{

    public static void main ( String[] args )
    {

        ImageJGroovyJob imageJGroovyJob = new ImageJGroovyJob();

        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings();
        sshConnectorSettings.user = "tischer";
        sshConnectorSettings.password = "OlexOlex";
        sshConnectorSettings.host = SSHConnectorSettings.EMBL_SLURM_HOST;

        SlurmExecutorService executorService = new SlurmExecutorService( sshConnectorSettings );
        executorService.submit( imageJGroovyJob );


    }
}
