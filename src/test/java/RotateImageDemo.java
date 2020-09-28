import de.embl.cba.cluster.JobSubmitter;
import de.embl.cba.cluster.JobFuture;
import de.embl.cba.cluster.JobSettings;
import de.embl.cba.cluster.develop.RotateImageCommand;
import ij.IJ;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;
import org.scijava.widget.TextWidget;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Plugin(type = Command.class, menuPath = "Plugins>EMBL>Test" )
public class RotateImageDemo implements Command
{

    @Parameter
    public UIService uiService;

    @Parameter
    public DatasetService datasetService;

    @Parameter
    public LogService logService;

    @Parameter
    public ThreadService threadService;

    @Parameter
    public OpService opService;

    @Parameter
    public StatusService statusService;

    @Parameter
    public CommandService commandService;


    @Parameter(label = "Password", style = TextWidget.PASSWORD_STYLE, persist = false, required = true )
    private String password;

    public void run()
    {
        try
        {

            Map<String, Object> parameters = new HashMap<>( );
            parameters.put( RotateImageCommand.INPUT_IMAGE_PATH, new File( "/Users/tischer/Documents/fiji-slurm/src/test/resources/horizontal-line.tif" ) );
            parameters.put( RotateImageCommand.ROTATION_ANGLE, 50 );
            parameters.put( RotateImageCommand.OUTPUT_IMAGE_PATH, new File( "/Users/tischer/Documents/fiji-slurm/src/test/resources/horizontal-line-rot.tif" ) );
            parameters.put( RotateImageCommand.QUIT_AFTER_RUN, false );

            //
            // run with current ij
            //
            /*
            logService.setLevel( LogLevel.DEBUG );

            Future ij2Future = commandService.run( RotateImageCommand.class, false, parameters );
            Object object = ij2Future.get(); // TODO: It does not return...


            logService.info(" Done in main. " );
            */

            //
            // run on this computer with other imagej instance
            //
            /*
            Future< HashMap< String, Object > > future;
            HashMap< String, Object> results;

            parameters.put( RotateImageCommand.QUIT_AFTER_RUN, true );
            future = submitOnLocalMac( RotateImageCommand.PLUGIN_NAME, parameters );
            results = future.get();
            IJ.log( (String) results.get( JobFuture.STD_OUT ) );
            */

            //
            // run on EMBL Slurm cluster
            //


            Future< HashMap< String, Object > > future;
            HashMap< String, Object> results;


            parameters.put( RotateImageCommand.INPUT_IMAGE_PATH, new File( "/g/cba/tischer/projects/cluster-development/horizontal-line.tif" ) );
            parameters.put( RotateImageCommand.ROTATION_ANGLE, 50 );
            parameters.put( RotateImageCommand.OUTPUT_IMAGE_PATH, new File( "/g/cba/tischer/projects/cluster-development/horizontal-line-rot.tif" ) );
            parameters.put( RotateImageCommand.QUIT_AFTER_RUN, true );

            future = submitOnEmblSlurm( RotateImageCommand.PLUGIN_NAME, parameters );
            results = future.get();
            IJ.log( (String) results.get( JobFuture.STD_OUT ) );



        }
        catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
        catch ( ExecutionException e )
        {
            e.printStackTrace();
        }


    }

    private Future submitOnLocalMac( String pluginName, Map< String, Object > parameters )
    {
        Future future;
        JobSubmitter commandsSubmitter = new JobSubmitter(
                        JobSubmitter.EXECUTION_SYSTEM_MAC_OS_LOCALHOST,
                        "/Users/tischer/Documents/tmp",
                        JobSubmitter.IMAGEJ_EXECUTABLE_MAC_OS,
                        "tischer", password );

        commandsSubmitter.addIJCommandWithParameters( pluginName, parameters );

        JobSettings jobSettings = new JobSettings();
        jobSettings.memoryPerJobInMegaByte = 16000;
        jobSettings.numWorkersPerNode = 4;
        jobSettings.queue = JobSettings.DEFAULT_QUEUE;
        future = commandsSubmitter.submitJob( jobSettings );

        return future;
    }

    private Future submitOnEmblSlurm( String pluginName, Map< String, Object > parameters )
    {

        JobSubmitter commandsSubmitter = new JobSubmitter(
            JobSubmitter.EXECUTION_SYSTEM_EMBL_SLURM,
            "/g/cba/cluster/tischer",
            JobSubmitter.IMAGEJ_EXECTUABLE_ALMF_CLUSTER_XVFB,
            "tischer", password );

        commandsSubmitter.addIJCommandWithParameters( pluginName, parameters );

        JobSettings jobSettings = new JobSettings();
        jobSettings.memoryPerJobInMegaByte = 16000;
        jobSettings.numWorkersPerNode = 4;
        jobSettings.queue = JobSettings.DEFAULT_QUEUE;
        return commandsSubmitter.submitJob( jobSettings );
    }


    public static void main ( String[] args ) throws IOException
    {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        ij.command().run( RotateImageDemo.class, true );
    }

}
