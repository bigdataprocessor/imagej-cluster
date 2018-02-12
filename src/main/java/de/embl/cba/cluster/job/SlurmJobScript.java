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

    private ArrayList< String > executableCommands;

    public SlurmJobScript( ArrayList< String > executableCommands, int memoryPerJobInMegaByte, int numWorkersPerNode, String slurmQueue )
    {
        this.executableCommands = executableCommands;
        this.memoryPerJobInMegaByte = 16000;
        this.numWorkersPerNode = 4;
        this.queue = slurmQueue;
    }

    public String getJobText( SSHExecutorService SSHExecutorService )
    {

        ArrayList < String > lines = new ArrayList< >(  );

        lines.add( "#!/bin/bash" );
        lines.add( "#SBATCH -e " + SSHExecutorService.getCurrentJobErrPath() );
        lines.add( "#SBATCH -o " + SSHExecutorService.getCurrentJobOutPath() );
        lines.add( "#SBATCH -N 1" );
        lines.add( "#SBATCH -n " + numWorkersPerNode );
        lines.add( "#SBATCH --mem " + memoryPerJobInMegaByte );
        lines.add( "#SBATCH -p " + queue );
        lines.add( "ulimit -c 0" );

        lines.add( SSHExecutorService.getJobStartedCommand() );

        for ( String r : executableCommands ) lines.add ( r );

        lines.add( SSHExecutorService.getJobFinishedCommand() );

        return String.join( "\n", lines );

    }

}
