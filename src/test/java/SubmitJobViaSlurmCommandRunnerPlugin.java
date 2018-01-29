import de.embl.cba.cluster.plugins.SlurmCommandRunnerPlugin;

import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.TextWidget;

import java.util.HashMap;
import java.util.Map;

import static de.embl.cba.cluster.job.ImageJCommandSlurmJob.ALMF_CLUSTER_IMAGEJ_HEADLESS;
import static de.embl.cba.cluster.job.ImageJCommandSlurmJob.ALMF_CLUSTER_IMAGEJ_XVFB;

@Plugin(type = Command.class, menuPath = "Plugins>EMBL>Test" )
public class SubmitJobViaSlurmCommandRunnerPlugin implements Command
{

    @Parameter(label = "Username", required = true )
    private String username;

    @Parameter(label = "Password", style = TextWidget.PASSWORD_STYLE, persist = false, required = true )
    private String password;

    public void run()
    {

        String command = createExecutableCommand();

        Map< String, Object > parameters = configureParameterMap( command, username, password );

        runCommandOnCluster( parameters );

    }

    private static void runCommandOnCluster( Map< String, Object > parameters )
    {
        ImageJ ij = new ImageJ();
        ij.command().run( SlurmCommandRunnerPlugin.class, false, parameters );
    }

    private static Map< String, Object > configureParameterMap( String command,
                                                                String username,
                                                                String password )
    {
        Map< String, Object > parameters = new HashMap<>(  );
        parameters.put( "username", username );
        parameters.put( "password", password );
        parameters.put( "commandAndParameters", command );
        return parameters;
    }

    private static String createExecutableCommand()
    {
        String ij2commmand = "\"Log Text\""; // name as appears in Fiji menu, NOT class name!
        String parameters = "\"text='Hello World!'\"";
        String cmd = ALMF_CLUSTER_IMAGEJ_HEADLESS + " " + ij2commmand + " " + parameters;
        return cmd;
    }


    public static void main(final String... args) throws Exception
    {
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        ij.command().run( SubmitJobViaSlurmCommandRunnerPlugin.class, true );

    }

}
