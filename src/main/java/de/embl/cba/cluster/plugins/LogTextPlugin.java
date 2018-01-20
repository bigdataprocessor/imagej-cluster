package de.embl.cba.cluster.plugins;

import ij.IJ;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>EMBL>Log Text" )
public class LogTextPlugin implements Command
{
    @Parameter(label = "Please enter some text", required = true )
    public String text;

    public void run()
    {
        printCommandToLogWindow();

        IJ.log( "You entered: " + text );
    }

    private void printCommandToLogWindow()
    {
        String plugin = getClass().getEnclosingClass().getName();
        IJ.log( "[Command] " + plugin + " " + "\"text=\""+text+"\"");
    }

}