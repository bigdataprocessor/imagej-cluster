package de.embl.cba.cluster.job;

import de.embl.cba.cluster.JobSettings;
import de.embl.cba.cluster.SSHExecutorService;

import java.util.ArrayList;


/**
 * https://wiki.embl.de/cluster/Env#Queues
 */
public class SlurmJobScript implements JobScript
{

    private final JobSettings jobSettings;
    public static final String XVFB_ERR_PATH = "XVFB_ERR_PATH";

    private ArrayList< String > executableCommands;

    public SlurmJobScript( ArrayList< String > executableCommands, JobSettings jobSettings )
    {
        this.executableCommands = executableCommands;
        this.jobSettings = jobSettings;
    }

    public String getJobText( SSHExecutorService sshExecutorService )
    {

        ArrayList < String > lines = new ArrayList< >(  );

        lines.add( "#!/bin/bash" );
        lines.add( "#SBATCH -e " + sshExecutorService.getCurrentJobErrPath() );
        lines.add( "#SBATCH -o " + sshExecutorService.getCurrentJobOutPath() );
        lines.add( "#SBATCH -N 1" );
        lines.add( "#SBATCH -n " + jobSettings.numWorkersPerNode );
        lines.add( "#SBATCH --mem " + jobSettings.memoryPerJobInMegaByte );
        lines.add( "#SBATCH -p " + jobSettings.queue );
        lines.add( "#SBATCH -t " + jobSettings.timePerJobInMinutes );
        lines.add( "ulimit -c 0" );

        lines.add( sshExecutorService.getJobStartedCommand() );

        for ( String executableCommand : executableCommands )
        {
            if ( executableCommand.contains( XVFB_ERR_PATH  ) )
            {
                executableCommand = executableCommand.replace(  XVFB_ERR_PATH, sshExecutorService.getCurrentXvfbErrPath() );
            }
            lines.add( executableCommand );
        }

        lines.add( sshExecutorService.getJobFinishedCommand() );

        return String.join( "\n", lines );

    }

}
