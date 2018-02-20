package de.embl.cba.cluster.job;

import de.embl.cba.cluster.SSHExecutorService;
import de.embl.cba.cluster.SlurmQueue;

import java.util.ArrayList;


/**
 * https://wiki.embl.de/cluster/Env#Queues
 */
public class SlurmJobScript implements JobScript
{

    public long memoryPerJobInMegaByte;
    public long numWorkersPerNode;
    public String queue;
    public static final String XVFB_ERR_PATH = "XVFB_ERR_PATH";

    private ArrayList< String > executableCommands;

    public SlurmJobScript( ArrayList< String > executableCommands, int memoryPerJobInMegaByte, int numWorkersPerNode, String slurmQueue )
    {
        this.executableCommands = executableCommands;
        this.memoryPerJobInMegaByte = memoryPerJobInMegaByte;
        this.numWorkersPerNode = numWorkersPerNode;
        this.queue = slurmQueue;
    }

    public String getJobText( SSHExecutorService sshExecutorService )
    {

        ArrayList < String > lines = new ArrayList< >(  );

        lines.add( "#!/bin/bash" );
        lines.add( "#SBATCH -e " + sshExecutorService.getCurrentJobErrPath() );
        lines.add( "#SBATCH -o " + sshExecutorService.getCurrentJobOutPath() );
        lines.add( "#SBATCH -N 1" );
        lines.add( "#SBATCH -n " + numWorkersPerNode );
        lines.add( "#SBATCH --mem " + memoryPerJobInMegaByte );
        lines.add( "#SBATCH -p " + queue );
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
