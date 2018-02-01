import de.embl.cba.cluster.job.ImageJGroovyScriptSlurmJob;
import de.embl.cba.cluster.job.Job;
import de.embl.cba.cluster.ssh.SSHConnectorSettings;
import de.embl.cba.cluster.SSHExecutorService;
import de.embl.cba.cluster.JobFuture;

import java.io.File;
import java.io.IOException;

public class GroovyIJ2Plugin_rotateImageBy50degrees
{

    public static void main ( String[] args ) throws IOException
    {

        ImageJGroovyScriptSlurmJob scriptJob = new ImageJGroovyScriptSlurmJob();
        scriptJob.setLocalGroovyScript(  new File("/Users/tischer/Documents/fiji-slurm/src/test/resources/ij2plugin-rotate-image.groovy" ) );
        scriptJob.setLocalInputImage( new File( "/Users/tischer/Documents/fiji-slurm/src/test/resources/horizontal-line.tif" ) );

        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings( "tischer", "pwd", SSHConnectorSettings.EMBL_SLURM_HOST );

        SSHExecutorService executorService = new SSHExecutorService( sshConnectorSettings );
        JobFuture future = executorService.submit( (Job ) scriptJob );

        for ( int i = 0; i < 10; ++i )
        {
            String status = future.getStatus();
            System.out.print( "Job getStatus: " + status );
        }

    }
}
