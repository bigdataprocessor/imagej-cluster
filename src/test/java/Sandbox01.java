import de.embl.cba.cluster.ExecutableImageJGroovyScript;
import de.embl.cba.cluster.SlurmExecutorService;
import de.embl.cba.cluster.SlurmJobScript;
import de.embl.cba.cluster.SSHConnectorSettings;

public class Sandbox01
{

    private static String remoteJobDirectoryAsMountedRemotely = "/g/cba/cluster/jobs";
    private static String remoteJobDirectoryAsMountedLocally = "/Volumes/cba/cluster/jobs";


    public static void main ( String[] args )
    {

        // TODO: split up below logic even more: separate the script from the ImageJ
        ExecutableImageJGroovyScript executableScript = new ExecutableImageJGroovyScript( );
        executableScript.imageJDirectory = ExecutableImageJGroovyScript.IMAGEJ_DIRECTORY_ALMF_LINUX;
        executableScript.relativeImageJBinaryPath = ExecutableImageJGroovyScript.RELATIVE_IMAGE_BINARY_PATH_ALMF_LINUX;
        executableScript.scriptPath = "/Users/tischer/Documents/fiji-slurm/src/test/resources/hello-world.groovy";

        SlurmJobScript jobScript = new SlurmJobScript();
        jobScript.memoryPerJobInMegaByte = "10000";
        jobScript.executableCommands = executableScript.getExecutableCommands();

        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings();
        sshConnectorSettings.user = "tischer";
        sshConnectorSettings.password = "OlexOlex";
        sshConnectorSettings.host = SSHConnectorSettings.EMBL_SLURM_HOST;

        SlurmExecutorService executorService = new SlurmExecutorService( sshConnectorSettings, remoteJobDirectoryAsMountedLocally, remoteJobDirectoryAsMountedRemotely );
        executorService.submit( jobScript );


    }
}
