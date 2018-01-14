import de.embl.cba.cluster.ImageJGroovyScriptJob;
import de.embl.cba.cluster.SSHConnectorSettings;
import de.embl.cba.cluster.SlurmExecutorService;
import de.embl.cba.cluster.SlurmJobFuture;

import java.io.File;
import java.io.IOException;

public class GroovyIJ2Plugin_rotateImageBy50degrees
{

    public static void main ( String[] args ) throws IOException
    {

        ImageJGroovyScriptJob scriptJob = new ImageJGroovyScriptJob();
        scriptJob.setLocalGroovyScript(  new File("/Users/tischer/Documents/fiji-slurm/src/test/resources/ij2plugin-rotate-image.groovy" ) );
        scriptJob.setLocalInputImage( new File( "/Users/tischer/Documents/fiji-slurm/src/test/resources/horizontal-line.tif" ) );

        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings();
        sshConnectorSettings.user = "tischer";
        sshConnectorSettings.password = "pwd";
        sshConnectorSettings.host = SSHConnectorSettings.EMBL_SLURM_HOST;

        SlurmExecutorService executorService = new SlurmExecutorService( sshConnectorSettings );
        SlurmJobFuture future = executorService.submit( scriptJob );

        for ( int i = 0; i < 10; ++i )
        {
            String status = future.status();
            System.out.print( "Job status: " + status );
        }

    }
}
