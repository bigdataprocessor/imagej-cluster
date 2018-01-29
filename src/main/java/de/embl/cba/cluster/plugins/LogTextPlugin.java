package de.embl.cba.cluster.plugins;

import ij.IJ;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>Institute Specific>EMBL>Log Text Test" )
public class LogTextPlugin implements Command
{
    @Parameter(label = "Please enter some text", required = true )
    public String text = "Hello World!";

    public void run()
    {
        IJ.log( "You entered: " + text );
    }

}