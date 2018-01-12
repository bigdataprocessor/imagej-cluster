import de.embl.cba.cluster.ExecutableImageJGroovyScript;
import de.embl.cba.cluster.SlurmExecutorService;
import de.embl.cba.cluster.SlurmJobScript;
import de.embl.cba.cluster.SlurmLoginSettings;

public class Sandbox01
{
    public static void main ( String[] args )
    {
        SlurmLoginSettings loginSettings = new SlurmLoginSettings();
        loginSettings.password = "password";
        SlurmExecutorService executorService = new SlurmExecutorService( loginSettings );

        ExecutableImageJGroovyScript executableScript = new ExecutableImageJGroovyScript( );
        executableScript.imageJDirectory = ExecutableImageJGroovyScript.IMAGEJ_DIRECTORY_ALMF_LINUX;
        executableScript.relativeImageJBinaryPath = ExecutableImageJGroovyScript.RELATIVE_IMAGE_BINARY_PATH_ALMF_LINUX;

        SlurmJobScript jobScript = new SlurmJobScript();
        jobScript.memoryPerJobInMegaByte = "10000";
        jobScript.executableCommands = executableScript.getExecutableCommands();

        executorService.submit(  )


    }
}
