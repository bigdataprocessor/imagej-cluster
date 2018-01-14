import de.embl.cba.cluster.*;

import java.io.IOException;

public class GroovyIJ2Plugin_LogText
{

    public static void main ( String[] args ) throws IOException
    {

        ImageJGroovyScriptJob imageJGroovyScriptJob = new ImageJGroovyScriptJob();
        imageJGroovyScriptJob.setLocalDependencyGroovyScriptNameAndText( "logText.groovy", Utils.readTextFile( "/Users/tischer/Documents/fiji-slurm/src/test/resources", "IJ2plugin_logText.groovy" ) );

        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings();
        sshConnectorSettings.user = "tischer";
        sshConnectorSettings.password = "OlexOlex";
        sshConnectorSettings.host = SSHConnectorSettings.EMBL_SLURM_HOST;

        SlurmExecutorService executorService = new SlurmExecutorService( sshConnectorSettings );
        executorService.submit( imageJGroovyScriptJob );

    }
}
