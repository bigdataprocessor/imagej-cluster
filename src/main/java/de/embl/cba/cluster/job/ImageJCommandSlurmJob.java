package de.embl.cba.cluster.job;

import de.embl.cba.cluster.SlurmExecutorService;

import java.util.ArrayList;

public class ImageJCommandSlurmJob implements SlurmJob
{

    public static final String ALMF_CLUSTER_IMAGEJ_XVFB = "xvfb-run -a /g/almf/software/Fiji.app/ImageJ-linux64 --ij2 --run";
    public static final String ALMF_CLUSTER_IMAGEJ_HEADLESS = "/g/almf/software/Fiji.app/ImageJ-linux64 --ij2 --headless --run";
    public static final String CBA_CLUSTER_IMAGEJ = "xvfb-run -a /g/cba/software/Fiji.app/ImageJ-linux64 --ij2 --run";

    private ArrayList< String > commands;

    public ImageJCommandSlurmJob( ArrayList< String > commands )
    {
        this.commands = commands;
    }

    @Override
    public String getJobText( SlurmExecutorService slurmExecutorService )
    {
        SlurmJobScript slurmJobScript = new SlurmJobScript();

        slurmJobScript.addExecutableCommand( "module load Java" );
        slurmJobScript.addExecutableCommand( "module load X11" );

        for ( String command : commands )
        {
            slurmJobScript.addExecutableCommand( command );
        }

        return slurmJobScript.jobText( slurmExecutorService );

    }


}
