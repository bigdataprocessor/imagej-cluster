package de.embl.cba.cluster;

import ij.IJ;

public class Logger
{

    public static void log( String text )
    {
        IJ.log( text );
    }


    public static void done( )
    {
        IJ.log( "done." );
    }


}
