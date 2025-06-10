/*-
 * #%L
 * Submitting and monitoring ImageJ jobs on a computer cluster
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.cluster;

import org.scijava.log.LogService;

import java.util.Iterator;
import java.util.Map;

public abstract class Commands
{

    public static String createImageJPluginCommandLineCall( String ImageJcmd, String pluginName, Map< String, Object > parameters )
    {
        String command = addImageJExecutable( ImageJcmd );

        command = addCommandName( pluginName, command );

        command = addParameters( parameters, command );

        return command;
    }


    public static String createCommandAndParameterString( String command, Map< String, Object > parameters )
    {
        String fullCommand = "";

        fullCommand = addCommandName( command, "" );

        fullCommand = addParameters( parameters, fullCommand );

        return fullCommand;
    }

    private static String addCommandName( String pluginName, String command )
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

    public static void quitImageJ( LogService logService )
    {
        logService.info( "Quitting ImageJ" );
        System.exit( 0 );
    }

    public static void quitImageJ( )
    {
        System.out.println( "Quitting ImageJ" );
        System.exit( 0 );
    }
}
