package de.embl.cba.cluster;

import de.embl.cba.cluster.logger.Logger;

import java.io.IOException;

public abstract class SlurmJobStatus
{
    public static final String COMPLETED = "COMPLETED";

    public static void monitorJobStatusAndShowOutAndErrWhenDone( SlurmJobFuture future ) throws IOException
    {
        for ( ; ; )
        {
            String status = logJobStatus( future );

            if ( status.equals( COMPLETED ) )
            {
                logJobError( future );
                logJobOutput( future );
                break;
            }
        }
    }

    private static void logJobOutput( SlurmJobFuture future ) throws IOException
    {
        Logger.log( future.getOutput() );
    }

    private static void logJobError( SlurmJobFuture future ) throws IOException
    {
        Logger.log( future.getError() );
    }

    private static String logJobStatus( SlurmJobFuture future )
    {
        String status = future.status();
        Logger.log( "Status of job " + future.jobID + " is " + status + "\n" );
        return status;
    }
}
