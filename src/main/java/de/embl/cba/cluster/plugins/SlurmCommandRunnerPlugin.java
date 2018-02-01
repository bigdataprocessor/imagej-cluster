package de.embl.cba.cluster.plugins;

import de.embl.cba.cluster.ImageJJobSubmitter;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.TextWidget;

import java.util.ArrayList;

@Plugin(type = Command.class, menuPath = "Plugins>Institute Specific>EMBL>Slurm Cluster Command Runner" )
public class SlurmCommandRunnerPlugin implements Command
{
    @Parameter(label = "User name" )
    private String username;

    @Parameter(label = "Password", style = TextWidget.PASSWORD_STYLE )
    private String password;

    @Parameter(label = "Command 0" )
    private String command0 = "";

    @Parameter(label = "Command 1", required = false)
    private String command1 = "";

    @Parameter(label = "Command 2", required = false)
    private String command2 = "";


    public void run()
    {

        ArrayList< String > commands = new ArrayList<>(  );
        commands.add( command0 );
        commands.add( command1 );
        commands.add( command2 );

        ImageJJobSubmitter.submit( commands, username, password );

    }

}
