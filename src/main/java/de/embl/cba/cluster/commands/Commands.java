package de.embl.cba.cluster.commands;

import ij.IJ;
import ij.ImageJ;

import java.util.Iterator;
import java.util.Map;

public abstract class Commands
{

    public static String createImageJPluginCommandLineCall( String ImageJcmd, String pluginName, Map< String, Object > parameters )
    {
        String command = addImageJExecutable( ImageJcmd );

        command = addPluginName( pluginName, command );

        command = addParameters( parameters, command );

        return command;
    }


    public static String createCommandAndParameterString( String command, Map< String, Object > parameters )
    {
        String fullCommand = "";

        fullCommand = addPluginName( command, "" );

        fullCommand = addParameters( parameters, fullCommand );

        return fullCommand;
    }

    private static String addPluginName( String pluginName, String command )
    {
        command += " \"" + pluginName + "\"";
        return command;
    }

    private static String addImageJExecutable( String ImageJcmd )
    {
        return ImageJcmd;
    }

    private static String addParameters( Map< String, Object > parameters, String command )
    {
        command += " \"";
        Iterator< String > keys = parameters.keySet().iterator();
        while ( keys.hasNext() )
        {
            String key = addImageJExecutable( keys.next() );
            command += key + "=" + "'" + parameters.get( key ) + "'";
            if ( keys.hasNext() ) command += ",";
        }
        command += "\" ";
        return command;
    }

    static void quitImageJ()
    {
        ImageJ ij = IJ.getInstance();
        if (ij!=null) ij.quit();
    }
}