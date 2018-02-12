package de.embl.cba.cluster;

import de.embl.cba.cluster.commands.Commands;
import de.embl.cba.cluster.job.JobScript;
import de.embl.cba.cluster.job.SimpleLinuxJobScript;
import de.embl.cba.cluster.job.SlurmJobScript;
import de.embl.cba.cluster.ssh.SSHConnector;
import de.embl.cba.cluster.ssh.SSHConnectorConfig;

import java.util.ArrayList;
import java.util.Map;

public class ImageJCommandsSubmitter
{
    public static final String EXECUTION_SYSTEM_EMBL_SLURM = "EMBL Slurm Cluster";
    public static final String EXECUTION_SYSTEM_MAC_OS_LOCALHOST = "MacOS localhost";

    public static final String IMAGEJ_EXECTUABLE_ALMF_CLUSTER_XVFB = "xvfb-run -a /g/almf/software/Fiji.app/ImageJ-linux64 --run";
    public static final String IMAGEJ_EXECTUABLE_ALMF_CLUSTER_HEADLESS = "/g/almf/software/Fiji.app/ImageJ-linux64 --ij2 --headless --run";
    public static final String IMAGEJ_EXECUTABLE_CBA_CLUSTER_XVFB = "xvfb-run -a /g/cba/software/Fiji.app/ImageJ-linux64 --run";
    public static final String IMAGEJ_EXECUTABLE_MAC_OS = "/Applications/Fiji.app/Contents/MacOS/ImageJ-macosx --run";


    private String executionSystem;
    private String remoteImageJExectuable;

    private String username;
    private String password;
    private String remoteJobDirectory;

    private ArrayList< String > commands;
    private SSHExecutorService sshExecutorService;

    public ImageJCommandsSubmitter( String executionSystem, String remoteJobDirectory, String remoteImageJExectuable, String username, String password )
    {
        this.executionSystem = executionSystem;
        this.remoteImageJExectuable = remoteImageJExectuable;
        this.username = username;
        this.password = password;
        this.remoteJobDirectory = remoteJobDirectory;

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

    public void addLinuxCommand( String command )
    {
        commands.add( command );
    }


    public JobFuture submitCommands( int memoryPerJobInMegaByte, int numWorkersPerNode, String slurmQueue )
    {

        if ( executionSystem.equals( EXECUTION_SYSTEM_EMBL_SLURM ) )
        {
            commands.add(0, "module load Java" );
            commands.add(0, "module load X11" );
        }

        JobScript jobScript = createJobScript( commands, memoryPerJobInMegaByte, numWorkersPerNode, slurmQueue);

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
            String hostname = "";
            String jobSubmissionType = "";

            if ( executionSystem.equals( EXECUTION_SYSTEM_EMBL_SLURM ) )
            {
                hostname = SSHConnectorConfig.EMBL_SLURM_HOST;
                jobSubmissionType = SSHExecutorService.SLURM_JOB;

            } else if ( executionSystem.equals( EXECUTION_SYSTEM_MAC_OS_LOCALHOST ) )
            {
                hostname = SSHConnectorConfig.LOCALHOST;
                jobSubmissionType = SSHExecutorService.LINUX_JOB;
            }

            SSHConnectorConfig sshConnectorConfig = new SSHConnectorConfig( username, password, hostname );
            SSHConnector sshConnector = new SSHConnector( sshConnectorConfig );

            sshExecutorService = new SSHExecutorService( sshConnector, remoteJobDirectory, jobSubmissionType );
        }

        return sshExecutorService;
    }

    private JobScript createJobScript( ArrayList< String > completeCommands, int memoryPerJobInMegaByte, int numWorkersPerNode, String slurmQueue  )
    {
        if ( executionSystem.equals( EXECUTION_SYSTEM_EMBL_SLURM ) )
        {
            JobScript jobScript = new SlurmJobScript( completeCommands, memoryPerJobInMegaByte, numWorkersPerNode, slurmQueue );
            return jobScript;
        }
        else if ( executionSystem.equals( EXECUTION_SYSTEM_MAC_OS_LOCALHOST ) )
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
        return remoteImageJExectuable + " " + command ;
    }

}
