package de.embl.cba.cluster.logger;

import ij.IJ;

public class Logger
{

    public static void log( String text )
    {
        IJ.log( text );
    }

    public static void error( String text )
    {
        IJ.showMessage( text );
    }

    public static void done( )
    {
        IJ.log( "done." );
    }


}
