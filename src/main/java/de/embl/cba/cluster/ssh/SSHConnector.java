package de.embl.cba.cluster.ssh;

import com.jcraft.jsch.*;
import de.embl.cba.cluster.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import static com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE;


// TODO: saveTextAsFile -> how to do the error handling?

public class SSHConnector
{
    private SSHConnectorConfig loginSettings;

    private Session session;
    private ChannelSftp channelSftp;
    private ChannelExec channelExec;

    private ArrayList< String > systemOut;
    private ArrayList< String > systemErr;

    public static final String OUTPUT = "out";
    public static final String ERROR = "err";

    public static final String IO_EXCEPTION = "IO_Exception";
    public static final String SFTP_EXCEPTION = "SFTP_Exception";

    public SSHConnector( SSHConnectorConfig loginSettings )
    {
        this.loginSettings = loginSettings;
    }

    public String userName()
    {
        return loginSettings.getUser();
    }

    private boolean connectSession()
    {
        Utils.logger.info( "Establishing SSH connection to " + loginSettings.getHost() + "...");

        JSch jsch = new JSch();
        try
        {
            session = jsch.getSession( loginSettings.getUser(), loginSettings.getHost(), loginSettings.port );
            session.setPassword( loginSettings.getPassword() );
            session.setConfig( "StrictHostKeyChecking", "no" );
            session.connect();
            return true;
        }
        catch ( JSchException e )
        {
           Utils.logger.error( "Could not connect to remote server!\n" +
                   "Probably username and/or password were not correct." );
           return false;
        }

    }

    private void connectChannelExec()
    {

        if ( session == null )
        {
            connectSession();
        }

        if ( channelExec == null || channelExec.isClosed() )
        {
            try
            {
                channelExec = ( ChannelExec ) session.openChannel( "exec" );
            }
            catch ( JSchException e )
            {
                e.printStackTrace();
            }
        }

    }

    private void connectChannelSftp()
    {
        if ( session == null )
        {
            connectSession();
        }

        if ( channelSftp == null )
        {
            try
            {
                Utils.logger.info( "Establishing SFTP connection to " + loginSettings.getHost() + "...");
                channelSftp = ( ChannelSftp ) session.openChannel( "sftp" );
                channelSftp.connect();
            }
            catch ( JSchException e )
            {
                e.printStackTrace();
            }
        }
    }


    public String ls( String directory )
    {
        HashMap< String, ArrayList<String> >  responses = executeCommand( "ls -la " + directory );

        return String.join( "\n" , responses.get( SSHConnector.OUTPUT ) );
    }

    public HashMap< String, ArrayList<String> > executeCommand( String command )
    {
        //Utils.logger.info( "# Executing remote command: " + command );

        if ( session == null )
        {
            connectSession();
        }

        connectChannelExec();
        channelExec.setCommand( command );

        HashMap< String, ArrayList<String> > systemResponse = recordSystemResponses();
        //HashMap< String, ArrayList<String> > systemResponse = null;

        // TODO: maybe wait there? or do not disconnect??
        channelExec.disconnect();

        return systemResponse;
    }


    private HashMap< String, ArrayList<String> > recordSystemResponses()
    {

        try
        {

            InputStream out = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();

            channelExec.connect(); // this appears to be necessary at exactly this position to collect both out and err

            String output = asString( out, channelExec );
            String error = asString( err, channelExec );

            systemOut = asListOfLines( output );
            systemErr = asListOfLines( error );

            HashMap< String, ArrayList<String> > systemResponses = new HashMap<>();
            systemResponses.put( OUTPUT, systemOut );
            systemResponses.put( ERROR, systemErr );

            channelExec.disconnect();

            return systemResponses;

        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        catch ( JSchException e )
        {
            e.printStackTrace();
        }

        return null;
    }

    public String remoteFileSeparator()
    {
        return "/";
    }

    private ArrayList< String > asListOfLines( String output )
    {
        ArrayList< String > listOfLines = new ArrayList<>(  );
        String[] strings = output.split( "\n" );
        for ( String string : strings )
        {
            listOfLines.add( string );
        }
        return listOfLines;

    }

    public boolean fileExists( String path )
    {

        Vector result = null;

        try
        {
            connectChannelSftp();
            result = channelSftp.ls( path );
        }
        catch ( SftpException e )
        {
            if (e.id == SSH_FX_NO_SUCH_FILE)
            {
                return false;
            }
        }

        return result != null && !result.isEmpty();
    }

    public void saveTextAsFileOnRemoteServerUsingSFTP( String text,
                                                       String directory,
                                                       String filename )
    {
        try
        {
            Utils.logger.info( "Saving remote file:\n" + directory + "/" + filename );

            connectChannelSftp();
            channelSftp.cd( directory );
            channelSftp.put( asInputStream( text ), filename );
        }
        catch ( SftpException e )
        {
            e.printStackTrace();
        }
        catch ( UnsupportedEncodingException e )
        {
            e.printStackTrace();
        }
    }

    public void rename( String oldPath, String newPath )
    {
        try
        {
            //Utils.logger.info( "# Renaming remote file: "  + oldPath + " to " + newPath );

            connectChannelSftp();

            channelSftp.rename( oldPath, newPath  );
        }
        catch ( SftpException e )
        {
            Utils.logger.warning( e.toString() );
        }
    }


    public String readRemoteTextFileUsingSFTP( String remoteDirectory, String remoteFileName )
    {
        // Utils.logger.info( "# Reading from remote file: " + remoteDirectory + "/" + remoteFileName );

        try
        {
            connectChannelSftp();

            channelSftp.cd( remoteDirectory );
            InputStream inputStream = channelSftp.get( remoteFileName );
            String text = asString( inputStream, channelSftp );

            return text;

        }
        catch ( IOException e )
        {
            Utils.logger.warning( e.toString() );
            return IO_EXCEPTION;
        }
        catch ( SftpException e )
        {
            Utils.logger.warning( e.toString() );
            return SFTP_EXCEPTION;
        }

    }


    private InputStream asInputStream( String text ) throws UnsupportedEncodingException
    {
        return new ByteArrayInputStream( text.getBytes( StandardCharsets.UTF_8.name() ) );
    }

    private static String asString( InputStream in, Channel channel ) throws IOException
    {

        // TODO: fix this

        /*
        byte[] tmp=new byte[1024];
        while(true){
            while(in.available()>0){
                int i=in.read(tmp, 0, 1024);
                if(i<0)break;
                System.out.print(new String(tmp, 0, i));
            }
            if(channel.isClosed()){
                if(in.available()>0) continue;
                System.out.println("exit-status: "+channel.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception ee){}
        }
        */



        if (in != null)
        {

            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            Reader reader = new BufferedReader( new InputStreamReader( in, "UTF-8") );
            int n;

            while(true)
            {
                while ( ( n = reader.read(buffer) ) != -1)
                {
                    writer.write(buffer, 0, n);
                }

                if ( n == -1 ) break;

                if( channel.isClosed() )
                {
                    if( in.available() > 0 ) continue;
                    break;
                }

                try{ Thread.sleep(1000); } catch( Exception ee ){}

            }

            in.close();

            return writer.toString();
        }
        else
        {
            return "";
        }

    }

}
