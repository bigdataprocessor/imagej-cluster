package de.embl.cba.cluster.ssh;

import com.jcraft.jsch.*;
import de.embl.cba.cluster.logger.Logger;

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

    private ChannelExec channelExec;
    private Session session;
    private ArrayList< String > systemOut;
    private ArrayList< String > systemErr;

    public static final String OUTPUT = "out";
    public static final String ERROR = "err";

    public SSHConnector( SSHConnectorConfig loginSettings )
    {
        this.loginSettings = loginSettings;
    }

    public String userName()
    {
        return loginSettings.getUser();
    }

    private void connectSession()
    {
        //Logger.log( "Establishing SSH connection to " + loginSettings.getHost() + "...");

        JSch jsch = new JSch();
        try
        {
            session = jsch.getSession( loginSettings.getUser(), loginSettings.getHost(), loginSettings.port );
            session.setPassword( loginSettings.getPassword() );
            session.setConfig( "StrictHostKeyChecking", "no" );
            session.connect();
        }
        catch ( JSchException e )
        {
            e.printStackTrace();
        }
        //Logger.done();
    }


    public String ls( String directory )
    {
        HashMap< String, ArrayList<String> >  responses = executeCommand( "ls -la " + directory );

        return String.join( "\n" , responses.get( SSHConnector.OUTPUT ) );
    }

    public HashMap< String, ArrayList<String> > executeCommand( String command )
    {
        Logger.log( "# Executing remote command: " + command );
        connectSession();

        execute( command );
        HashMap< String, ArrayList<String> > systemResponse = recordSystemResponseText();

        disconnect();

        return systemResponse;
    }

    private void disconnect()
    {
        channelExec.disconnect();
        session.disconnect();
    }

    private HashMap< String, ArrayList<String> > recordSystemResponseText()
    {
        InputStream out = null;
        try
        {
            out = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();

            channelExec.connect();

            String output = asString( out );
            String error = asString( err );

            systemOut = asListOfLines( output );
            systemErr = asListOfLines( error );

            HashMap< String, ArrayList<String> > systemResponses = new HashMap<>();
            systemResponses.put( OUTPUT, systemOut );
            systemResponses.put( ERROR, systemErr );
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
        // TODO
        return File.separator;
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

    private void execute( String command )
    {
        try
        {
            channelExec = ( ChannelExec ) session.openChannel( "exec" );
            channelExec.setCommand( command );
        }
        catch ( JSchException e )
        {
            e.printStackTrace();
        }

    }

    public boolean fileExists( String path )
    {

        Vector result = null;
        ChannelSftp channelSftp = null;

        try {
            channelSftp = createSftpChannel();
            result = channelSftp.ls( path );
        }
        catch (SftpException e) {
            if (e.id == SSH_FX_NO_SUCH_FILE)
            {
                channelSftp.disconnect();
                return false;
            }
        }
        catch ( JSchException e )
        {
            e.printStackTrace();
            return false;
        }

        channelSftp.disconnect();

        return result != null && !result.isEmpty();
    }

    public void saveTextAsFileOnRemoteServerUsingSFTP( String text,
                                                       String directory,
                                                       String filename )
    {
        try
        {
            Logger.log( "# Saving text as remote file:");
            Logger.log( "Remote file path: " + directory + "/" + filename );
            Logger.log( "Text: " );
            Logger.log( text );

            ChannelSftp channelSftp = createSftpChannel();
            channelSftp.cd( directory );
            channelSftp.put( asInputStream( text ), filename );
            channelSftp.disconnect();

        }
        catch ( JSchException e )
        {
            e.printStackTrace();
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
            Logger.log( "# Renaming remote file:" );
            Logger.log( "Original path: " + oldPath );
            Logger.log( "New path: " + newPath );

            ChannelSftp channelSftp = createSftpChannel();

            channelSftp.rename( oldPath, newPath  );

            channelSftp.disconnect();

        }
        catch ( JSchException e )
        {
            Logger.error( e.toString() );
        }
        catch ( SftpException e )
        {
            Logger.error( e.toString() );
        }
    }

    private ChannelSftp createSftpChannel() throws JSchException
    {
        connectSession();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        return channelSftp;
    }


    public String readRemoteTextFileUsingSFTP( String remoteDirectory, String remoteFileName )
    {
        Logger.log( "# Reading from remote file: " + remoteDirectory + "/" + remoteFileName );

        try
        {
            ChannelSftp channelSftp = createSftpChannel();

            channelSftp.cd( remoteDirectory );
            InputStream inputStream = channelSftp.get( remoteFileName );
            String text = asString( inputStream );

            channelSftp.disconnect();

            return text;

        }
        catch ( JSchException e )
        {
            Logger.error( e.toString() );
        }
        catch ( IOException e )
        {
            Logger.error( e.toString() );
        }
        catch ( SftpException e )
        {
            Logger.error( e.toString() );
        }

        return "Error reading file...";

    }


    private InputStream asInputStream( String text ) throws UnsupportedEncodingException
    {
        return new ByteArrayInputStream( text.getBytes( StandardCharsets.UTF_8.name() ) );
    }

    private static String asString( InputStream is ) throws IOException
    {

        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        }
        else {
            return "";
        }
    }

}
