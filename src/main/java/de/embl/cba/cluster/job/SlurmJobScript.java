package de.embl.cba.cluster.job;

import de.embl.cba.cluster.JobSettings;
import de.embl.cba.cluster.SSHExecutorService;

import java.util.ArrayList;


/**
 * https://wiki.embl.de/cluster/Env#Queues
 */
public class SlurmJobScript implements JobScript
{
    public static final String DO_NOT_ECHO = " # Do not print";
    private final JobSettings jobSettings;
    public static final String XVFB_ERR_PATH = "XVFB_ERR_PATH";

    private ArrayList< String > executableCommands;

    public SlurmJobScript( ArrayList< String > executableCommands, JobSettings jobSettings )
    {
        this.executableCommands = executableCommands;
        this.jobSettings = jobSettings;
    }

    public String getJobText( SSHExecutorService sshExecutorService, String jobID )
    {
        ArrayList < String > lines = new ArrayList< >(  );

        lines.add( "#!/bin/bash" );
        lines.add( "#SBATCH -e " + sshExecutorService.getJobErrPath( jobID ) );
        lines.add( "#SBATCH -o " + sshExecutorService.getJobOutPath( jobID ) );
        lines.add( "#SBATCH -N 1" );
        lines.add( "#SBATCH -n 1" );
        lines.add( "#SBATCH -c " + jobSettings.numWorkersPerNode );
        lines.add( "#SBATCH --mem " + jobSettings.memoryPerJobInMegaByte );
        lines.add( "#SBATCH -p " + jobSettings.queue );
        lines.add( "#SBATCH -t " + jobSettings.timePerJobInMinutes );

        lines.add( "ulimit -c 0" );

        lines.add( sshExecutorService.getJobStartedCommand( jobID ) );

        lines.add( "date" );
        lines.add( "echo \"job started\"" );

        for ( String executableCommand : executableCommands )
        {
            if ( executableCommand.contains( XVFB_ERR_PATH  ) )
            {
                executableCommand = executableCommand.replace(  XVFB_ERR_PATH, sshExecutorService.getJobXvfbErrPath( jobID ) );
            }

            // execution time of the command
            lines.add( "date" );

            // potentially echo the command
            if ( ! executableCommand.contains( DO_NOT_ECHO ) )
            {
                final String printableCommand = executableCommand.replace( "(", "\\(" ).replace( ")", "\\)" );
                lines.add( "echo \"" + printableCommand + "\"" );
            }
            else
            {
                executableCommand = executableCommand.replace( DO_NOT_ECHO, "" );
            }

            // add the actual command
            lines.add( executableCommand );
        }

        lines.add( sshExecutorService.getJobFinishedCommand( jobID ) );

        return String.join( "\n", lines );

    }

}
