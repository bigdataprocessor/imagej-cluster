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

import de.embl.cba.cluster.job.JobScript;
import de.embl.cba.cluster.ssh.SSHConnector;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

// TODO: set remote tmp dir via some default mechanism
// TODO: option to submit job just as long remoteRootDirectory (easier than file)
// TODO: how to do the manageJobDependencies such that it is generic?
// TODO: implement logic of whether remote system is windows or linux

// https://www.rc.fas.harvard.edu/resources/documentation/convenient-slurm-commands/

public class SSHExecutorService implements ExecutorService
{
    // input
    private final SSHConnector sshConnector;

    public static final String SLURM_JOB = "Slurm";
    public static final String LINUX_JOB = "Linux";

    public static final String SLURM_JOB_SUBMISSION_COMMAND = "sbatch ";
    public static final String LINUX_JOB_SUBMISSION_COMMAND = "";

    private final String SLURM_JOB_STATUS_COMMAND = "sacct -j ";
    private final String SLURM_SUCCESSFUL_JOB_SUBMISSION_RESPONSE = "Submitted batch job ";

    private final String COULD_NOT_DETERMINE_JOB_STATUS = "Could not determine job status";

    public static final String OUTPUT = ".out";
    public static final String ERROR = ".err";
    public static final String XVFB_ERROR = ".xvfb.err";

    public static final String STARTED = ".started";
    public static final String FINISHED = ".finished";
    public static final String JOB = ".job";

    private String jobDirectory;
    private JobExecutor.ScriptType scriptType;
    private String dateTime;
    private int numJobsSubmitted;

    // Commands
    private String submitJobCommand;
    private String pipeOutputCommand;
    private String makeRemoteDirectoryCommand;
    private String makeScriptExecutableCommand;
    private String createEmptyFileCommand;

    public SSHExecutorService( SSHConnector sshConnector, String jobDirectory, JobExecutor.ScriptType scriptType )
    {
        setDateAndTime();

        this.sshConnector = sshConnector;

        this.jobDirectory = jobDirectory + "/" + dateTime;

        this.scriptType = scriptType;

        numJobsSubmitted = 0;

        makeRemoteDirectoryCommand = "mkdir -p ";

        makeScriptExecutableCommand = "chmod +x ";

        createEmptyFileCommand = "touch ";
    }

    private void setDateAndTime()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddhhmmss");
        dateTime = simpleDateFormat.format( new Date() );
    }

    public void shutdown()
    {

    }

    public List< Runnable > shutdownNow()
    {
        return null;
    }

    public boolean isShutdown()
    {
        return false;
    }

    public boolean isTerminated()
    {
        return false;
    }

    public boolean awaitTermination( long timeout, TimeUnit unit ) throws InterruptedException
    {
        return false;
    }

    public < T > Future< T > submit( Callable< T > task )
    {
        return null;
    }

    public < T > Future< T > submit( Runnable task, T result )
    {
        return null;
    }

    public Future< ? > submit( Runnable runnable )
    {
        // ISSUE: I could not make the Runnable concept work, as I had no good idea
        // what the .run() method show do in a cluster context
        return null;
    }

    public synchronized JobFuture submit( JobScript jobScript )
    {
        String jobID = getJobID();

        return submit( jobScript, jobID );
    }


    public synchronized JobFuture submit( JobScript jobScript, String jobID )
    {
        ensureExistenceOfRemoteJobDirectory( );

        createRemoteJobFile( jobID, jobScript );

        submitJob( jobID );

        return createJobFuture( jobID, jobScript );
    }

    private JobFuture createJobFuture( String jobID, JobScript jobScript )
    {
        JobFuture jobFuture = new JobFuture( this, jobID, jobScript );

        return jobFuture;
    }

    public < T > List< Future< T > > invokeAll( Collection< ? extends Callable< T > > tasks ) throws InterruptedException
    {
        return null;
    }

    public < T > List< Future< T > > invokeAll( Collection< ? extends Callable< T > > tasks, long timeout, TimeUnit unit ) throws InterruptedException
    {
        return null;
    }

    public < T > T invokeAny( Collection< ? extends Callable< T > > tasks ) throws InterruptedException, ExecutionException
    {
        return null;
    }

    public < T > T invokeAny( Collection< ? extends Callable< T > > tasks, long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
    {
        return null;
    }

    public void execute( Runnable runnable )
    {
    }

    private void ensureExistenceOfRemoteJobDirectory()
    {
        try
        {
            //Utils.logger.info( "Remote directory:\n" + jobDirectory );
            sshConnector.executeCommand( makeRemoteDirectoryCommand + jobDirectory );
        }
        catch ( Exception e )
        {
            Utils.logger.error( e.toString() );
        }
    }

    public String getJobOutput( String jobID )
    {
        return sshConnector.readRemoteTextFileUsingSFTP( jobDirectory, getJobOutFilename( jobID )  );
    }

    public String getJobError( String jobID )
    {
        return sshConnector.readRemoteTextFileUsingSFTP( jobDirectory, getJobErrFilename( jobID )  );
    }

    private HashMap< String, ArrayList< String > > submitJob( String jobID )
    {
        HashMap< String, ArrayList< String > > responses = sshConnector.executeCommand( getJobSubmissionCommand( jobID ) );
        return responses;
    }

    private String getJobSubmissionCommand( String jobID )
    {
        String jobSubmissionCommand = "";

        if ( scriptType.equals( JobExecutor.ScriptType.SlurmJob ) )
        {
            jobSubmissionCommand = SLURM_JOB_SUBMISSION_COMMAND;
        }
        else if ( scriptType.equals( JobExecutor.ScriptType.LinuxShell  ) )
        {
            jobSubmissionCommand = LINUX_JOB_SUBMISSION_COMMAND;
        }

        jobSubmissionCommand += jobDirectory + sshConnector.remoteFileSeparator() + getJobFilename( jobID );

        if ( scriptType.equals(  JobExecutor.ScriptType.SlurmJob ) )
        {
            //
        }
        else if ( scriptType.equals( JobExecutor.ScriptType.LinuxShell ) )
        {
            jobSubmissionCommand += " > " + getJobOutPath( jobID ) + " &";
        }

        return jobSubmissionCommand;
    }

    public boolean isFinished( String jobID )
    {
        return sshConnector.fileExists( jobDirectory + sshConnector.remoteFileSeparator() + getJobFinishedFilename( jobID ) );
    }

    public boolean isStarted( String jobID )
    {
        return sshConnector.fileExists( jobDirectory + sshConnector.remoteFileSeparator() + getJobStartedFilename( jobID ) );
    }

    private String getJobFinishedFilename( String jobID )
    {
        return jobID + FINISHED;
    }

    private String getJobStartedFilename( String jobID )
    {
        return ( jobID + STARTED );
    }

    private void createRemoteJobFile( String jobID, JobScript jobScript )
    {
        String jobText = jobScript.getJobText( this, jobID );
        sshConnector.saveTextAsFileOnRemoteServerUsingSFTP( jobText, jobDirectory, getJobFilename( jobID ) );
        Utils.logger.info( "Output file:\n" + jobDirectory + "/" + getJobOutFilename( jobID ) );
        Utils.logger.info( "Error file:\n" + jobDirectory + "/" + getJobErrFilename( jobID ) );

        //sshConnector.executeCommand( makeScriptExecutableCommand + jobDirectory + sshConnector.remoteFileSeparator() + getJobFilename( jobID ) );
    }

    private String getJobID()
    {
        String jobID = String.format( "%05d", numJobsSubmitted );
        
        numJobsSubmitted++;
        
        return jobID;
    }

    public String getJobDirectory()
    {
        return jobDirectory;
    }

    public String getJobXvfbErrPath( String jobID )
    {
        return jobDirectory + sshConnector.remoteFileSeparator() + getJobXvfbErrFilename( jobID  );
    }

    public String getJobOutPath( String jobID )
    {
        return jobDirectory + sshConnector.remoteFileSeparator() + getJobOutFilename( jobID  );
    }

    public String getJobErrPath( String jobID )
    {
        return jobDirectory + sshConnector.remoteFileSeparator() + getJobErrFilename( jobID  );
    }

    public String getJobStartedCommand( String jobID )
    {
        return createEmptyFileCommand + getJobStartedPath( jobID );
    }

    public String getJobFinishedCommand( String jobID )
    {
        return createEmptyFileCommand + getJobFinishedPath( jobID  ) ;
    }

    public String getJobFinishedPath( String jobID  )
    {
        return jobDirectory + sshConnector.remoteFileSeparator() + getJobFinishedFilename( jobID );
    }

    public String getJobStartedPath( String jobID  )
    {
        return jobDirectory + sshConnector.remoteFileSeparator() + getJobStartedFilename( jobID ) ;
    }

    public String getJobXvfbErrFilename( String jobID  )
    {
        return jobID + XVFB_ERROR ;
    }

    public String getJobOutFilename( String jobID  )
    {
        return jobID + OUTPUT ;
    }

    public String getJobErrFilename( String jobID  )
    {
        return jobID + ERROR ;
    }

    private String getJobFilename( String jobID )
    {
        return jobID + JOB;
    }

    public SSHConnector getSshConnector()
    {
        return sshConnector;
    }
}
