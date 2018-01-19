import de.embl.cba.cluster.plugins.SlurmCommandRunnerPlugin;
import net.imagej.ImageJ;

import java.util.HashMap;
import java.util.Map;

public class SubmitJobViaSlurmCommandRunnerPlugin
{

    public static void main ( String[] args )
    {

        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        Map< String, Object > parameters = new HashMap<>(  );
        parameters.put( "username", "tischer" );
        parameters.put( "password", "OlexOlex" );
        parameters.put( "commandAndParameters", "test" );

        ij.command().run( SlurmCommandRunnerPlugin.class, false, parameters );

    }


}
