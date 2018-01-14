import de.embl.cba.cluster.*;

import java.io.File;
import java.io.IOException;

public class GroovyIJ2Plugin_logText
{

    public static void main ( String[] args ) throws IOException
    {

        ImageJGroovyScriptJob imageJGroovyScriptJob = new ImageJGroovyScriptJob();
        imageJGroovyScriptJob.setLocalGroovyScript( new File( "/Users/tischer/Documents/fiji-slurm/src/test/resources/IJ2plugin_logText.groovy" ));

        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings();
        sshConnectorSettings.user = "tischer";
        sshConnectorSettings.password = "pwd";
        sshConnectorSettings.host = SSHConnectorSettings.EMBL_SLURM_HOST;

        SlurmExecutorService executorService = new SlurmExecutorService( sshConnectorSettings );
        SlurmJobFuture future = executorService.submit( imageJGroovyScriptJob );

        for ( int i = 0; i < 10; ++i )
        {
            String status = future.status();
            System.out.print( "Job status: " + status );
        }

    }

}
