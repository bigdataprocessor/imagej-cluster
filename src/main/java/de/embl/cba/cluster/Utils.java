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


    public static void saveTextAsFile( String text, String path  )
    {
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


    public static void saveTextAsFile( String text, String filename, String directory )
    {
        String path = directory + File.separator + filename;
        saveTextAsFile( text, path );
    }

    public static String readTextFile( String directory, String fileName ) throws IOException
    {
        String path = directory + File.separator + fileName;

        return readTextFile( path );
    }

    public static String readTextFile( String path ) throws IOException
    {

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

    public static void waitMilliseconds( int millis )
    {
        try
        {
            Thread.sleep( millis );
        } catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }


    public static String getFullPathFromWindowsMappedDriveLetterPath( String mappedPath )
    {
        /*
        public static String getOsName()
        {
            if(OS == null) { OS = System.getProperty("os.name"); }
            return OS;
        }
        public static boolean isWindows()
        {
            return getOsName().startsWith("Windows");
        }
        */

        /*
        Runtime runTime = Runtime.getRuntime();
        Process process = null;
        try
        {
            process = runTime.exec("net use");
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        InputStream inStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = null;
        String[] components = null;
        String fullPath = null;
        while (null != (line = bufferedReader.readLine())) {
            components = line.split("\\s+");
            if ((components.length > 2) && (components[1].equals(mappedPath.substring(0, 2)))) {
                fullPath = mappedPath.replace(components[1], components[2]);
            }
        }

        return fullPath;
        */

        return null;
    }


}
