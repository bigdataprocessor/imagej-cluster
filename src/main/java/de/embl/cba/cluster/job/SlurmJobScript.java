package de.embl.cba.cluster.job;

import de.embl.cba.cluster.SlurmExecutorService;
import de.embl.cba.cluster.SlurmQueues;

import java.util.ArrayList;


/**
 * https://wiki.embl.de/cluster/Env#Queues
 */
public class SlurmJobScript
{

    public long memoryPerJobInMegaByte;
    public long numWorkersPerNode;
    public String queue;

    private ArrayList< String > executableCommands;

    public SlurmJobScript( )
    {
        this.executableCommands = new ArrayList<>( );
        memoryPerJobInMegaByte = 16000;
        numWorkersPerNode = 4;
        queue = SlurmQueues.DEFAULT_QUEUE;
    }

    public void addExecutableCommand( String command )
    {
        executableCommands.add( command );
    }

    public void setExecutableCommands( ArrayList< String > commands )
    {
        executableCommands = commands;
    }

    public String jobText( SlurmExecutorService slurmExecutorService )
    {

        ArrayList < String > lines = new ArrayList< >(  );

        lines.add( "#!/bin/bash" );
        lines.add( "#SBATCH -e " + slurmExecutorService.getCurrentJobErrPath() );
        lines.add( "#SBATCH -o " + slurmExecutorService.getCurrentJobOutPath() );
        lines.add( "#SBATCH -N 1" );
        lines.add( "#SBATCH -n " + numWorkersPerNode );
        lines.add( "#SBATCH --mem " + memoryPerJobInMegaByte );
        lines.add( "#SBATCH -p " + queue );
        lines.add( " " );
        lines.add( "# Node = %N" );
        lines.add( "# Job = %j" );
        lines.add( "ulimit -c 0" );
        lines.add( " " );
        lines.add( "echo \"job started\" \n" );
        lines.add( " " );
        for ( String r : executableCommands ) lines.add ( r );

        lines.add( " " );
        lines.add( "echo \"job finished\" \n" );

        return String.join( "\n", lines );

    }

}
