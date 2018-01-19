import de.embl.cba.cluster.plugins.SlurmCommandRunnerPlugin;
import net.imagej.ImageJ;

import java.util.HashMap;
import java.util.Map;

import static de.embl.cba.cluster.job.ImageJCommandSlurmJob.ALMF_CLUSTER_IMAGEJ_CMD;

public class SubmitJobViaSlurmCommandRunnerPlugin
{

    public static void main ( String[] args )
    {

        final ImageJ ij = startImageJ();

        String command = createExecutableCommand();

        Map< String, Object > parameters = configureParameterMap( command );

        runCommandOnCluster( ij, parameters );

    }

    private static void runCommandOnCluster( ImageJ ij, Map< String, Object > parameters )
    {
        ij.command().run( SlurmCommandRunnerPlugin.class, false, parameters );
    }

    private static Map< String, Object > configureParameterMap( String command )
    {
        Map< String, Object > parameters = new HashMap<>(  );
        parameters.put( "username", "tischer" );
        parameters.put( "password", "pwd" );
        parameters.put( "commandAndParameters", command );
        return parameters;
    }

    private static String createExecutableCommand()
    {
        String imagej = ALMF_CLUSTER_IMAGEJ_CMD;

        String ij2commmand = "LogTextPlugin";

        String parameters = "text=\"Hello World!\"";

        String cmd = imagej + " " + ij2commmand + " " + parameters;

        return cmd;
    }

    private static ImageJ startImageJ()
    {
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();
        return ij;
    }


}
