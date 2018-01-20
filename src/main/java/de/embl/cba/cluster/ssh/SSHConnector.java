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

    private void connectSession() throws JSchException
    {
        //Logger.log( "Establishing SSH connection to " + loginSettings.getHost() + "...");

        JSch jsch = new JSch();
        session = jsch.getSession( loginSettings.getUser(), loginSettings.getHost(), loginSettings.port );
        session.setPassword( loginSettings.getPassword() );
        session.setConfig( "StrictHostKeyChecking", "no" );
        session.connect();

        //Logger.done();
    }

    public ArrayList<String> executeCommand( String command ) throws Exception
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

    private void recordSystemResponseText() throws IOException, JSchException
    {
        InputStream out = channelExec.getInputStream();
        InputStream err = channelExec.getErrStream();

        channelExec.connect();

        String output = asString( out );
        String error = asString( err );

        systemResponses = new ArrayList< String >(  );
        addToSystemResponses( output );
        addToSystemResponses( error );
    }

    private void addToSystemResponses( String output )
    {
        String[] strings = output.split( "\n" );
        for ( String string : strings )
        {
            systemResponses.add( string );
        }
    }

    private void execute( String command ) throws JSchException
    {
        channelExec = ( ChannelExec ) session.openChannel( "exec" );
        channelExec.setCommand( command );
    }

    public void saveTextAsFileOnRemoteServerUsingSFTP( String text,
                                                       String remoteFileName,
                                                       String remoteDirectory ) throws Exception
    {
        connectSession();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        channelSftp.cd( remoteDirectory );
        channelSftp.put( asInputStream( text ), remoteFileName );

        channelSftp.disconnect();
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
                Reader reader = new BufferedReader(new InputStreamReader(is,
                        "UTF-8"));
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
