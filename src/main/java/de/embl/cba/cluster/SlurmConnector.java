package de.embl.cba.cluster;


import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;


public class SlurmConnector
{
    private SlurmLoginSettings loginSettings;
    private ChannelExec channelExec;
    private Session session;
    private String systemResponseText;


    public SlurmConnector( SlurmLoginSettings loginSettings )
    {
        this.loginSettings = loginSettings;
    }

    public String executeCommand( String command ) throws JSchException, IOException
    {

        connect();
        execute( command );
        recordSystemResposeText();
        disconnect();

        return systemResponseText;

    }

    private void disconnect()
    {
        channelExec.disconnect();
        session.disconnect();
    }

    private void recordSystemResposeText() throws IOException, JSchException
    {
        InputStream in = channelExec.getInputStream();
        channelExec.setErrStream( System.err );
        channelExec.connect();

        systemResponseText = convertStreamToStr( in );
        System.out.println( systemResponseText );
    }

    private void execute( String command ) throws JSchException
    {
        channelExec = ( ChannelExec ) session.openChannel( "exec" );
        channelExec.setCommand( command );
    }

    private void connect() throws JSchException
    {
        JSch jsch = new JSch();
        session = jsch.getSession( loginSettings.user, loginSettings.host, loginSettings.port );
        session.setPassword( loginSettings.password );
        session.setConfig( "StrictHostKeyChecking", "no" );
        System.out.println( "Establishing Connection..." );
        session.connect();
        System.out.println( "Connection established." );
    }


    private static String convertStreamToStr(InputStream is) throws IOException
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
