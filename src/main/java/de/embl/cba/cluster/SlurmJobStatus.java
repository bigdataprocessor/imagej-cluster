package de.embl.cba.cluster;

import java.io.IOException;

public abstract class SlurmJobStatus
{
    public static final String COMPLETED = " COMPLETED";

    public static void monitorJobStatusAndShowOutAndErrWhenDone( SlurmJobFuture future ) throws IOException
    {
        for ( int i = 0; i < 100; ++i )
        {
            System.out.print( "Status of job " + future.jobID + " is " + future.status() + "\n" );

            if ( future.status().equals( COMPLETED ) )
            {
                Logger.log( "Job " + future.jobID + " is done." );

                Logger.log( future.getError() );

                Logger.log( future.getOutput() );

                break;
            }
        }
    }
}
