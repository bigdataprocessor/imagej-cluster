package de.embl.cba.cluster.ssh;

public class SSHConnectorSettings
{
    public static final String EMBL_SLURM_HOST = "login.cluster.embl.de";


    private String user = "tischer";
    private String password = "password";
    private String host = EMBL_SLURM_HOST;
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


    public SSHConnectorSettings( String user, String password, String host )
    {
        this.user = user;
        this.password = password;
        this.host = host;
    }


}
