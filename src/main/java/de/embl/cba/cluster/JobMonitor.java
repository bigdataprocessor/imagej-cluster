/*-
 * #%L
 * Submitting and monitoring ImageJ jobs on a computer cluster
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.cluster;

import de.embl.cba.cluster.log.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JobMonitor
{
    Logger logger;
    Status currentStatus;
    private List< JobFuture > jobFutures;
    private long startTimeMillis;

    public JobMonitor( Logger logger )
    {
        this.logger = logger;
    }

    public void monitorJobProgress( List< JobFuture > jobFutures, int monitoringIntervalInSeconds, int maxNumResubmissions )
    {
        this.startTimeMillis = System.currentTimeMillis();
        this.jobFutures = jobFutures;
        currentStatus = new Status();

        while ( ( currentStatus.numFinished + currentStatus.numFailed ) < jobFutures.size() )
        {
            logger.info( "Waiting for " + monitoringIntervalInSeconds + " seconds until providing an update on your jobs." );
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
        for ( JobFuture jobFuture : jobFutures )
        {
            // jobFuture.getOutput() TODO: provide some more information.
        }

        if ( status.numFailed > 0 )
        {
            logger.info( "All jobs finished (some have failed)." );
            logger.info( getTimeMessage() );
        }
        else
        {
            logger.info( "All jobs finished." );
            logger.info( getTimeMessage() );
        }

        logger.info( "JobMonitor is done." );
    }

    private void logJobStati( List< JobFuture > jobFutures, Status status )
    {
        logger.info( " " );
        logger.info( "# Job status summary" );
        logger.info( getTimeMessage() );
        logger.info( "Submitted: " + jobFutures.size() );
        logger.info( "Resubmitted: " + status.numResubmitted );
        logger.info( "Running: " + status.numRunning );
        logger.info( "Finished: " + status.numFinished );
        logger.info( "Failed: " + status.numFailed );
        logger.info( " " );
    }

    @NotNull
    private String getTimeMessage()
    {
        return "Time since start of monitoring [min]: " + getTimeSinceStartMinutes();
    }

    private int getTimeSinceStartMinutes()
    {
        return (int) ( Math.ceil( 1.0 * System.currentTimeMillis() - startTimeMillis ) / ( 60.0 * 1000) );
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
