import de.embl.cba.cluster.ssh.SSHConnector;
import de.embl.cba.cluster.ssh.SSHConnectorSettings;
import ij.IJ;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.TextWidget;

@Plugin(type = Command.class, menuPath = "Plugins>EMBL>Test" )
public class LocalSSH implements Command
{

    @Parameter(label = "Username", required = true )
    private String username;

    @Parameter(label = "Password", style = TextWidget.PASSWORD_STYLE, persist = false, required = true )
    private String password;


    public void run()
    {
        SSHConnectorSettings sshConnectorSettings = new SSHConnectorSettings( username, password, "localhost" );
        SSHConnector sshConnector = new SSHConnector( sshConnectorSettings );
        IJ.log( sshConnector.ls( "/Users/tischer" ) );
    }

    public static void main(final String... args) throws Exception
    {
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        ij.command().run( LocalSSH.class, true );
    }


}
