package de.embl.cba.cluster;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

public class SlurmJobScript
{

    public String memoryPerJobInMegaByte;
    public ArrayList< String > executableCommands;


    public void save( String directory, String filename ) throws Exception
    {
        String path = directory + File.pathSeparator + filename;
        PrintWriter writer = new PrintWriter( path, "UTF-8" );
        writer.write( getJobText() );
        writer.close();
    }

    public String getJobText()
    {
        ArrayList < String > lines = new ArrayList< String >(  );

        lines.add( "#!/bin/bash" );
        lines.add( "#SBATCH -N 1" );
        lines.add( "#SBATCH --mem " + memoryPerJobInMegaByte );
        lines.add( "#SBATCH -e slurm.%N.%j.err " );
        lines.add( "#SBATCH -o slurm.%N.%j.out " );

        for ( String r : executableCommands )
        {
            lines.add ( r );
        }

        return String.join( "\n", lines );

    }

}
