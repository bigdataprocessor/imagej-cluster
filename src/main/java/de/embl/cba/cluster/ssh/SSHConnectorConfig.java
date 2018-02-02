package de.embl.cba.cluster.ssh;

public class SSHConnectorConfig
{
    public static final String EMBL_SLURM_HOST = "login.cluster.embl.de";
    public static final String LOCALHOST = "localhost";

    private String user;
    private String password;
    private String host;
    public int port = 22;

    public String getHost()
    {
        return host;
    }

    public String getUser()
    {
        return user;
    }

    public String getPassword()
    {
        return password;
    }

    public SSHConnectorConfig( String user, String password, String host )
    {
        this.user = user;
        this.password = password;
        this.host = host;
    }


}
