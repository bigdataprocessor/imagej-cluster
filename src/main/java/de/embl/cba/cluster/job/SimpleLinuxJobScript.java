package de.embl.cba.cluster.job;

import de.embl.cba.cluster.SSHExecutorService;

import java.util.ArrayList;

public class SimpleLinuxJobScript implements JobScript
{
    private ArrayList< String > executableCommands;

    public SimpleLinuxJobScript( ArrayList< String > executableCommands )
    {
        this.executableCommands = executableCommands;
    }

    public String getJobText( SSHExecutorService SSHExecutorService, String jobID )
    {

        ArrayList < String > lines = new ArrayList< >(  );

        lines.add( "#!/bin/bash" );
//        lines.add( "#SBATCH -e " + SSHExecutorService.getCurrentJobErrPath() );
//        lines.add( "#SBATCH -o " + SSHExecutorService.getCurrentJobOutPath() );

        lines.add( SSHExecutorService.getJobStartedCommand( jobID ) );

        for ( String r : executableCommands ) lines.add ( r );

        lines.add( SSHExecutorService.getJobFinishedCommand( jobID ) );

        return String.join( "\n", lines );

    }
}
