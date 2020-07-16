import de.embl.cba.cluster.*;
import de.embl.cba.cluster.commands.PrintTextCommand;
import de.embl.cba.log.IJLazySwingLogger;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.TextWidget;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static de.embl.cba.cluster.ImageJCommandsSubmitter.IMAGEJ_EXECTUABLE_ALMF_CLUSTER_HEADLESS;

/* Simple test, just printing text onto command line */

@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>ClusterTest>Print Text On Cluster" )
public class PrintTextOnCluster implements Command
{
    @Parameter
    public LogService logService;

    @Parameter( label = "Username" )
    private String username;

    @Parameter( label = "Password", style = TextWidget.PASSWORD_STYLE, persist = false )
    private String password;

    @Parameter (label = "Job directory", style = "directory" )
    public File jobDirectory;

    @Parameter (label = "Some text"  )
    public String inputText = "Hello world!";

    @Parameter (label = "Number of jobs" )
    public int numJobs = 4;

    @Parameter (label = "Memory [MB]"  )
    public int memoryMB = 2000;

    @Parameter (label = "Number of threads per job" )
    public int numWorkers = 1;

    @Parameter (label = "Time per job in minutes" )
    public int timePerJobInMinutes = 3;


    IJLazySwingLogger logger = new IJLazySwingLogger();

    public void run()
    {

        logger.setLogService( logService );

        ArrayList< JobFuture > jobFutures = submitJobsOnSlurm(
                IMAGEJ_EXECTUABLE_ALMF_CLUSTER_HEADLESS,
                jobDirectory.toPath() );

        SlurmJobMonitor slurmJobMonitor = new SlurmJobMonitor( logger );

        slurmJobMonitor.monitorJobProgress(
                jobFutures,
                3,
                0 );

    }


    private ArrayList< JobFuture > submitJobsOnSlurm( String imageJ, Path jobDirectory )
    {

        ImageJCommandsSubmitter commandsSubmitter = getImageJCommandsSubmitter( imageJ, jobDirectory );

        JobSettings jobSettings = getJobSettings();

        ArrayList< JobFuture > jobFutures = new ArrayList<>( );

        for ( int i = 0; i < numJobs; ++i )
        {
            commandsSubmitter.clearCommands();
            setCommandAndParameterStrings( commandsSubmitter, inputText );
            jobFutures.add( commandsSubmitter.submitCommands( jobSettings ) );

            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }

        return jobFutures;
    }

    private JobSettings getJobSettings()
    {
        JobSettings jobSettings = new JobSettings();
        jobSettings.queue = JobSettings.DEFAULT_QUEUE;
        jobSettings.numWorkersPerNode = numWorkers;
        jobSettings.timePerJobInMinutes = timePerJobInMinutes;
        jobSettings.memoryPerJobInMegaByte = memoryMB;
        return jobSettings;
    }

    private ImageJCommandsSubmitter getImageJCommandsSubmitter( String imageJ, Path jobDirectory )
    {
        return new ImageJCommandsSubmitter(
                ImageJCommandsSubmitter.EXECUTION_SYSTEM_EMBL_SLURM,
                PathMapper.asEMBLClusterMounted( jobDirectory.toString() ),
                imageJ,
                username,
                password );
    }

    private void setCommandAndParameterStrings( ImageJCommandsSubmitter commandsSubmitter, String text )
    {
        Map< String, Object > parameters = new HashMap<>();
        parameters.clear();
        parameters.put( PrintTextCommand.INPUT_TEXT, inputText );
        parameters.put( PrintTextCommand.QUIT_AFTER_RUN, true );
        commandsSubmitter.addIJCommandWithParameters( PrintTextCommand.PLUGIN_NAME , parameters );
    }

    public static void main ( String[] args ) throws IOException
    {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        ij.command().run( PrintTextOnCluster.class, true );
    }

}
