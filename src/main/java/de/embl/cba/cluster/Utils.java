package de.embl.cba.cluster;

import java.io.*;
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

    public static String readTextFile( String directory, String fileName ) throws IOException
    {
        String path = directory + File.separator + fileName;

        BufferedReader br = new BufferedReader(new FileReader( path ) );
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }


    public static String getLastLine( String string )
    {
        String[] strings = string.split( "\n" );
        String lastLine = strings[ strings.length - 1 ];
        return lastLine;
    }

    public static String localMounting( String directory )
    {
        String localMounting = directory.replace( "/g/", "/Volumes/" );

        return localMounting;
    }
}
