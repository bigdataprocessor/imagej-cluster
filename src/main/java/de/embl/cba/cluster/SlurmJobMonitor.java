package de.embl.cba.cluster;

import de.embl.cba.log.Logger;

import java.util.ArrayList;

public class SlurmJobMonitor
{
    Logger logger;
    Status currentStatus;

    public SlurmJobMonitor( Logger logger )
    {
        this.logger = logger;
    }

    public void monitorJobProgress( ArrayList< JobFuture > jobFutures, int monitoringIntervalInSeconds, int maxNumResubmissions )
    {
        currentStatus = new Status();

        while ( ( currentStatus.numFinished + currentStatus.numFailed ) < jobFutures.size() )
        {
            sleep( monitoringIntervalInSeconds );

            currentStatus.numRunning = 0;
            currentStatus.numResubmitted = 0;
            currentStatus.numFailed = 0;
            currentStatus.numFinished = 0;

            for ( JobFuture jobFuture : jobFutures )
            {
                jobFuture.refreshStatus();

                logger.info( jobFuture.getJobID() + ": " + jobFuture.getStatusHistory() );

                String currentStatusString = jobFuture.getCurrentStatus();

                currentStatus.numResubmitted += jobFuture.getNumReSubmissions();

                if ( currentStatusString.equals( JobFuture.RUNNING ) )
                {
                    currentStatus.numRunning++;
                }
                else if ( currentStatusString.equals( JobFuture.ERROR ) )
                {
                    if ( jobFuture.getNumReSubmissions() < maxNumResubmissions )
                    {
                        jobFuture.resubmit();
                        currentStatus.numRunning++;
                    }
                    else
                    {
                        currentStatus.numFailed++;
                    }
                }
                else if ( currentStatusString.equals( JobFuture.FINISHED ) )
                {
                    currentStatus.numFinished++;
                }

            }

            logJobStati( jobFutures, currentStatus );

        }

        finalReport( currentStatus );

    }

    private void sleep( int monitoringIntervalInSeconds )
    {
        try { Thread.sleep( monitoringIntervalInSeconds * 1000 ); } catch ( InterruptedException e ) { e.printStackTrace(); }
    }

    private void finalReport( Status status )
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

    private void logJobStati( ArrayList< JobFuture > jobFutures, Status status )
    {
        logger.info( " " );
        logger.info( "# Current job status summary" );
        logger.info( "Submitted: " + jobFutures.size() );
        logger.info( "Started: " + status.numRunning );
        logger.info( "Finished: " + status.numFinished );
        logger.info( "Resubmitted: " + status.numResubmitted );
        logger.info( "Failed: " + status.numFailed );
        logger.info( " " );
    }

    class Status
    {
        int numRunning = 0;
        int numResubmitted = 0;
        int numFinished = 0;
        int numXvfbErrors = 0;
        int numSlurmStepErrors = 0;
        int numUnkownErrors = 0;
        int numFailed = 0;
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