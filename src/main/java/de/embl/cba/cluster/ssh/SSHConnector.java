package de.embl.cba.cluster.ssh;

import com.jcraft.jsch.*;
import de.embl.cba.cluster.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


// TODO: saveTextAsFile -> how to do the error handling?

public class SSHConnector
{
    private SSHConnectorSettings loginSettings;

    private ChannelExec channelExec;
    private Session session;
    private ArrayList< String > systemResponses;

    public SSHConnector( SSHConnectorSettings loginSettings )
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
        ArrayList< String > responses = executeCommand( "ls -la " + directory );

        return String.join( "\n" , responses );
    }

    public ArrayList<String> executeCommand( String command )
    {
        connectSession();

        execute( command );
        recordSystemResponseText();

        disconnect();

        return systemResponses;
    }

    private void disconnect()
    {
        channelExec.disconnect();
        session.disconnect();
    }

    private void recordSystemResponseText()
    {
        InputStream out = null;
        try
        {
            out = channelExec.getInputStream();
            InputStream err = channelExec.getErrStream();

            channelExec.connect();

            String output = asString( out );
            String error = asString( err );

            systemResponses = new ArrayList< String >(  );
            addToSystemResponses( output );
            addToSystemResponses( error );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        catch ( JSchException e )
        {
            e.printStackTrace();
        }
    }

    private void addToSystemResponses( String output )
    {
        String[] strings = output.split( "\n" );
        for ( String string : strings )
        {
            systemResponses.add( string );
        }
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

    public void saveTextAsFileOnRemoteServerUsingSFTP( String text,
                                                       String remoteDirectory,
                                                       String remoteFileName ) throws Exception
    {
        Logger.log( "Creating remote file: " + remoteDirectory + "/" + remoteFileName );
        ChannelSftp channelSftp = createSftpChannel();

        channelSftp.cd( remoteDirectory );
        channelSftp.put( asInputStream( text ), remoteFileName );

        channelSftp.disconnect();
    }

    public void rename( String oldPath, String newPath )
    {
        try
        {
            Logger.log( "Renaming remote file:" );
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
        Logger.log( "Reading from remote file: " + remoteDirectory + "/" + remoteFileName );

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
