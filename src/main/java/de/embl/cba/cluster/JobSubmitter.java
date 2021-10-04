/*-
 * #%L
 * Submitting and monitoring ImageJ jobs on a computer cluster
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
import de.embl.cba.cluster.job.SimpleLinuxJobScript;
import de.embl.cba.cluster.job.SlurmJobScript;
import de.embl.cba.cluster.ssh.SSHConnector;
import de.embl.cba.cluster.ssh.SSHConnectorConfig;

import java.util.ArrayList;
import java.util.Map;

import static de.embl.cba.cluster.job.SlurmJobScript.DO_NOT_ECHO;

public class JobSubmitter
{
    public static final String EXECUTION_SYSTEM_EMBL_SLURM = "EMBL Slurm Cluster";
    public static final String EXECUTION_SYSTEM_MAC_OS_LOCALHOST = "MacOS localhost";

    public static final String RUN_IJ_MACRO_OPTIONS = " --ij2 --allow-multiple --headless -eval";
    public static final String RUN_IJ_COMMAND_OPTIONS = " --mem=MEMORY_MB --ij2 --allow-multiple --headless --run";

    public static final String IMAGEJ_EXECTUABLE_ALMF_CLUSTER_XVFB = "xvfb-run -a -e XVFB_ERR_PATH /g/almf/software/Fiji.app/ImageJ-linux64 --allow-multiple --mem=MEMORY_MB --run";

    public static final String IMAGEJ_EXECTUABLE_ALMF_CLUSTER_HEADLESS = "/g/almf/software/Fiji.app/ImageJ-linux64" + RUN_IJ_COMMAND_OPTIONS;
    public static final String IMAGEJ_EXECTUABLE_ALMF_CLUSTER_HEADLESS_MACRO = "/g/almf/software/Fiji.app/ImageJ-linux64 " + RUN_IJ_MACRO_OPTIONS;

    public static final String IMAGEJ_EXECUTABLE_CBA_CLUSTER_XVFB = "xvfb-run -a /g/cba/software/Fiji.app/ImageJ-linux64 --mem=MEMORY_MB --allow-multiple --run";
    public static final String IMAGEJ_EXECUTABLE_MAC_OS = "/Applications/Fiji.app/Contents/MacOS/ImageJ-macosx --mem=MEMORY_MB --allow-multiple --run";

    @Deprecated
    private String executionSystem;

    private final JobExecutor executor;
    private String imageJExecutable;

    private String username;
    private String password;
    private String jobDirectory;

    private ArrayList< String > commands;
    private SSHExecutorService sshExecutorService;

    public JobSubmitter( JobExecutor executor,
                         String jobDirectory,
                         String imageJExecutable,
                         String username,
                         String password )
    {
        this.executor = executor;
        this.imageJExecutable = imageJExecutable;
        this.username = username;
        this.password = password;
        this.jobDirectory = jobDirectory;

        commands = new ArrayList<>();
    }

    public void clearCommands()
    {
        commands.clear();
    }

    public void addIJCommandWithParameters( String command, Map<String, Object> parameters )
    {
        String commandAndParameters = Commands.createCommandAndParameterString( command, parameters );
        String ijBinaryAndCommandAndParameters = prependIJBinary( commandAndParameters );
        commands.add( ijBinaryAndCommandAndParameters );
    }

    public void addIJMacroExecution( String macroString )
    {
        String quotedMacroString = " \'" + macroString + "\'";
        String ijExecutableAndMacroString = prependIJBinary( quotedMacroString );
        System.out.println( ijExecutableAndMacroString );
        commands.add( ijExecutableAndMacroString );
    }

    public void addLinuxCommand( String command )
    {
        commands.add( command );
    }

    /**
     *
     *
     *
     * @param jobSettings
     * @return
     */
    public JobFuture submitJob( JobSettings jobSettings )
    {
        ArrayList< String > finalCommands = new ArrayList<>();

        if ( executor.scriptType.equals( JobExecutor.ScriptType.SlurmJob ) )
        {
            finalCommands.add( "echo $SLURM_JOB_ID" + DO_NOT_ECHO);
            finalCommands.add( "hostname" );
            finalCommands.add( "lscpu" );
            finalCommands.add( "free -m" );
            finalCommands.add( "START_TIME=$SECONDS"  + DO_NOT_ECHO );

            // finalCommands.add( "module load Java" );
            // finalCommands.add( "module load X11" ); // only necessary when the job cannot run headless

            finalCommands.add( "mkdir -p ~/.imagej" );
            finalCommands.add( "sleep 1s" );

            finalCommands.add( "cp /g/almf/software/Fiji.app/IJ_Prefs.txt ~/.imagej/" );
            finalCommands.add( "sleep 1s" );
        }

        for ( String command : commands )
        {
            String finalCommand = command.replace( "MEMORY_MB", "" + jobSettings.memoryPerJobInMegaByte + "M" );
            finalCommands.add( finalCommand );
        }

        if ( executor.scriptType.equals( JobExecutor.ScriptType.SlurmJob ) )
        {
            finalCommands.add( "ELAPSED_TIME_IN_SECONDS=$(($SECONDS - $START_TIME))" + DO_NOT_ECHO );
            finalCommands.add( "echo \"Time spent in seconds $ELAPSED_TIME_IN_SECONDS\"" + DO_NOT_ECHO );
        }

        JobScript jobScript = createJobScript( finalCommands, jobSettings );

        JobFuture future = submitJobScript( jobScript );

        return future;
    }

    private JobFuture submitJobScript( JobScript jobScript )
    {
        SSHExecutorService executorService = getSSHExecutorService();

        return executorService.submit( jobScript );
    }

    private SSHExecutorService getSSHExecutorService()
    {
        if ( sshExecutorService == null )
        {
            SSHConnectorConfig sshConnectorConfig = new SSHConnectorConfig( username, password, executor.hostName );
            SSHConnector sshConnector = new SSHConnector( sshConnectorConfig );

            sshExecutorService = new SSHExecutorService(
                    sshConnector,
                    jobDirectory,
                    executor.scriptType );
        }

        return sshExecutorService;
    }

    private JobScript createJobScript( ArrayList< String > completeCommands, JobSettings jobSettings  )
    {
        if ( executor.scriptType.equals( JobExecutor.ScriptType.SlurmJob ) )
        {
            JobScript jobScript = new SlurmJobScript( completeCommands, jobSettings );
            return jobScript;
        }
        else if ( executor.scriptType.equals( JobExecutor.ScriptType.LinuxShell ) )
        {
            JobScript jobScript = new SimpleLinuxJobScript( completeCommands );
            return jobScript;
        }
        else
        {
            return null;
        }
    }

    private String prependIJBinary( String command )
    {
        return "time " + imageJExecutable + " " + command ;
    }
}
