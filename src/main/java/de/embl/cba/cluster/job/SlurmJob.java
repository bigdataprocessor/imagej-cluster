package de.embl.cba.cluster.job;

import de.embl.cba.cluster.SSHExecutorService;

import java.util.ArrayList;

public class SlurmJob implements Job
{
    private ArrayList< String > commands;

    public SlurmJob( ArrayList< String > commands )
    {
        this.commands = commands;
    }

    @Override
    public String getJobText( SSHExecutorService SSHExecutorService )
    {
        SlurmJobScript slurmJobScript = new SlurmJobScript();

        for ( String command : commands )
        {
            slurmJobScript.addExecutableCommand( command );
        }

        return slurmJobScript.jobText( SSHExecutorService );

    }


}
