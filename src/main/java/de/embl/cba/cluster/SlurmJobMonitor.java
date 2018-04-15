package de.embl.cba.cluster;

import de.embl.cba.utils.logging.Logger;

import java.util.ArrayList;

public class SlurmJobMonitor
{
    class Status
    {
        int numStarted = 0;
        int numResubmitted = 0;
        int numFinished = 0;
        int numXvfbErrors = 0;
        int numSlurmStepErrors = 0;
        int numUnkownErrors = 0;
        int numFailed = 0;
    }

    public void monitorJobProgress( ArrayList< JobFuture > jobFutures, Logger logger )
    {
        Status status = new Status();

        while ( ( status.numFinished + status.numFailed ) < jobFutures.size() )
        {
            try { Thread.sleep( 10000 ); } catch ( InterruptedException e ) { e.printStackTrace(); }

            status.numStarted = 0;
            status.numResubmitted = 0;
            status.numFailed = 0;
            status.numFinished = 0;

            for ( JobFuture jobFuture : jobFutures )
            {
                if ( jobFuture.isStarted() ) status.numStarted++;
                if ( jobFuture.isDone() ) status.numFinished++;
                if ( jobFuture.hasFailed() ) status.numFailed++;
                status.numResubmitted += jobFuture.getNumReSubmissions();

                String jobStatus = jobFuture.getStatus();

                logger.info( jobFuture.getJobID() + ": " + jobStatus );

            }

            logJobStati( jobFutures, logger, status );

        }

        finalReport( logger, status );

    }

    private void finalReport( Logger logger, Status status )
    {
        if ( status.numFailed > 0 )
        {
            logger.info( "All jobs finished (some have failed)." );
        }
        else
        {
            logger.info( "All jobs finished." );
        }
    }

    private static void logJobStati( ArrayList< JobFuture > jobFutures, Logger logger, Status status )
    {
        logger.info( " " );
        logger.info( "# Current job status summary" );
        logger.info( "Submitted: " + jobFutures.size() );
        logger.info( "Started: " + status.numStarted );
        logger.info( "Finished: " + status.numFinished );
        logger.info( "Resubmitted: " + status.numResubmitted );
        logger.info( "Failed (> 5 resubmissions): " + status.numFailed );
        logger.info( " " );
    }
}


/*
if ( jobFuture.isStarted() )
                {

                    String currentOutput = jobFuture.getOutput();

                    if  ( ! doneJobs.contains( jobFuture ) )
                    {
                        String[] currentOutputLines = currentOutput.split( "\n" );
                        String lastLine = currentOutputLines[ currentOutputLines.length - 1 ];
                        logger.info( "Current last line of job output: " + lastLine );
                    }

                    String resubmissionNeeded = jobFuture.needsResubmission();

                    if ( ! resubmissionNeeded.equals( JobFuture.NO_ERROR ) )
                    {
                        if ( resubmissionAttempts.containsKey( jobFuture ) )
                        {
                            int numResubmitted = resubmissionAttempts.get( jobFuture );
                            resubmissionAttempts.put( jobFuture, ++numResubmitted );
                        }
                        else
                        {
                            resubmissionAttempts.put( jobFuture, 1 );
                        }

                        if ( resubmissionAttempts.containsKey( jobFuture ) && resubmissionAttempts.get( jobFuture ) > 5 )
                        {
                            logger.info( "# Job failed more than 5 times. Will not resubmit." );
                            slurmErrors.numFailed++;
                        }
                        else
                        {
                            logger.info( "RESUBMITTING: " + jobFuture.getJobID() );
                            jobFuture.resubmit();

                            if ( resubmissionNeeded.equals( JobFuture.XVFB_ERROR ) )
                            {
                                slurmErrors.numXvfbErrors++;
                            }
                            else if ( resubmissionNeeded.equals( JobFuture.SLURM_STEP_ERROR ) )
                            {
                                slurmErrors.numSlurmStepErrors++;
                            }
                            else
                            {
                                logger.info( "UNKOWN ERROR: " + resubmissionNeeded );

                                slurmErrors.numUnkownErrors++;
                            }

                            slurmErrors.numResubmitted++;
                        }

                    }
                    else if ( jobFuture.isFinished() )
                    {
                        logger.info("Final and full job output:" );
                        logger.info( currentOutput );
                        doneJobs.add( jobFuture );
                        if ( doneJobs.size() == jobFutures.size() )
                        {
                            break;
                        }
                    }
                }
                else
                {
                    logger.info( "Job " + jobFuture.getJobID() + " has not yet started." );
                }

 */