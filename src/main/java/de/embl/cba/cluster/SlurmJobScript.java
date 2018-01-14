package de.embl.cba.cluster;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;


/**
 * https://wiki.embl.de/cluster/Env#Queues
 */
public class SlurmJobScript
{

    public long memoryPerJobInMegaByte;
    public long numWorkersPerNode;
    public String jobDirectory;
    public String queue;
    public String jobRemoteFilename;

    public ArrayList< String > executableCommands;

    public void save( String directory, String filename ) throws Exception
    {
        String path = directory + File.pathSeparator + filename;
        PrintWriter writer = new PrintWriter( path, "UTF-8" );
        writer.write( jobText() );
        writer.close();
    }

    public String jobText()
    {
        ArrayList < String > lines = new ArrayList< String >(  );

        lines.add( "#!/bin/bash" );
        lines.add( "#SBATCH -e " + jobDirectory + "/" + jobRemoteFilename + "--node_%N--id_%j.err" );
        lines.add( "#SBATCH -o " + jobDirectory + "/" + jobRemoteFilename + "--node_%N--id_%j.out" );
        lines.add( "#SBATCH -N 1" );
        lines.add( "#SBATCH -n " + numWorkersPerNode );
        lines.add( "#SBATCH --mem " + memoryPerJobInMegaByte );
        lines.add( "#SBATCH -p " + queue );
        lines.add( " " );
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
