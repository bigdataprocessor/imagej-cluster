package de.embl.cba.cluster;

import com.jcraft.jsch.*;

import java.io.*;


public class SSHConnector
{
    private SSHConnectorSettings loginSettings;

    private ChannelExec channelExec;
    private Session session;
    private String systemResponseText;
    private String localTmpDirectory;

    public SSHConnector( SSHConnectorSettings loginSettings )
    {
        this.loginSettings = loginSettings;
        localTmpDirectory = System.getProperty("java.io.tmpdir");
    }

    public String userName()
    {
        return loginSettings.user;
    }

    public void setLocalTmpDirectory( String localTmpDirectory )
    {
        this.localTmpDirectory = localTmpDirectory;
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


    public void saveTextAsFileOnRemoteServer( String text,
                                      String remoteFileName,
                                      String remoteDirecory ) throws Exception
    {


        String localTmpPath = localTmpDirectory + File.pathSeparator + remoteFileName;
        PrintWriter writer = new PrintWriter( localTmpPath, "UTF-8" );
        writer.write( text);
        writer.close();

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        channelSftp.lcd( localTmpDirectory );
        channelSftp.cd( remoteDirecory );
        channelSftp.put( remoteFileName, remoteFileName );


        channelSftp.disconnect();

    }


    public void copyFile()
    {
        System.out.println("Crating SFTP Channel.");
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();
        System.out.println("SFTP Channel created.");
        InputStream out = null;
        out = channelSftp.get(remoteFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(out));
        String line;
        while ((line = br.readLine()) != null)
            System.out.println(line);
        br.close();
        channelSftp.disconnect();

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
