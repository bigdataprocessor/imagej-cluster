package de.embl.cba.cluster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Utils
{

    static String timeStamp()
    {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        return timeStamp;
    }

    public static void saveTextAsFile( String text, String remoteFileName, String remoteDirectory )
    {
        String path = remoteDirectory + File.separator + remoteFileName;
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter( path, "UTF-8" );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( UnsupportedEncodingException e )
        {
            e.printStackTrace();
        }
        writer.write( text);
        writer.close();
    }

    public static String getLastLine( String string )
    {
        String[] strings = string.split( "\n" );
        String lastLine = strings[ strings.length - 1 ];
        return lastLine;
    }
}
