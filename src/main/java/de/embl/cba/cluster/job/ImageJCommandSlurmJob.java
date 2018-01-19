package de.embl.cba.cluster.job;

import de.embl.cba.cluster.SlurmExecutorService;

public class ImageJCommandSlurmJob implements SlurmJob
{

    private String commandAndParameters;

    public ImageJCommandSlurmJob( String commandAndParameters)
    {
        this.commandAndParameters = commandAndParameters;
    }

    @Override
    public String getJobText( SlurmExecutorService slurmExecutorService )
    {
        SlurmJobScript slurmJobScript = new SlurmJobScript();

        slurmJobScript.addExecutableCommand( "module load Java" );
        slurmJobScript.addExecutableCommand( "module load X11" );
        slurmJobScript.addExecutableCommand( commandAndParameters );

        return slurmJobScript.jobText( slurmExecutorService );

    }


}
