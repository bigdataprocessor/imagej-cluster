import de.embl.cba.cluster.*;
import de.embl.cba.cluster.job.ImageJGroovyScriptSlurmJob;
import de.embl.cba.cluster.job.Job;
import de.embl.cba.cluster.ssh.SSHConnectorSettings;

import java.io.File;
import java.io.IOException;

public class GroovyIJ2Plugin_logText
{

    public static void main ( String[] args ) throws IOException
    {

        ImageJGroovyScriptSlurmJob imageJGroovyScriptSlurmJob = new ImageJGroovyScriptSlurmJob();
        imageJGroovyScriptSlurmJob.setLocalGroovyScript( new File( "/Users/tischer/Documents/fiji-slurm/src/test/resources/IJ2plugin_logText.groovy" ));

        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings( "tischer", "pwd", SSHConnectorSettings.EMBL_SLURM_HOST );

        SSHExecutorService executorService = new SSHExecutorService( sshConnectorSettings );

        JobFuture future = executorService.submit( (Job ) imageJGroovyScriptSlurmJob );

        for ( int i = 0; i < 10; ++i )
        {
            String status = future.getStatus();
            System.out.print( "Job getStatus: " + status );
        }

    }

}
