package de.embl.cba.cluster;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ImageJGroovyJob
{
    public LinkedHashMap< String, Dependency > dependencies;
    public static final String JAVA_DEPENDENCY = "Java";
    public static final String X11_DEPENDENCY = "X11";

    private SlurmJobScript slurmJobScript;

    public ImageJGroovyJob(  )
    {
        configureJobScript();
        configureDependencies();
    }

    private void configureJobScript()
    {
        slurmJobScript = new SlurmJobScript();
        slurmJobScript.queue = SlurmQueues.DEFAULT_QUEUE;
        slurmJobScript.memoryPerJobInMegaByte = 10000;
        slurmJobScript.numWorkersPerNode = 4;
    }

    private void configureDependencies()
    {
        this.dependencies = new LinkedHashMap< String, Dependency >(  );

        addJavaDependency();
        addX11Dependency();
        // XVFB
        // IMAGEJ

    }

    public void setRemoteJobDirectory( String directory )
    {
        slurmJobScript.jobDirectory = directory;
    }

    private void addJavaDependency()
    {
        Dependency dependency = new Dependency();
        dependency.remoteType = DependencyType.SlurmModule;
        dependency.remoteObject = "module load Java";
        dependencies.put( JAVA_DEPENDENCY, dependency );
    }

    private void addX11Dependency()
    {
        Dependency dependency = new Dependency();
        dependency.remoteType = DependencyType.SlurmModule;
        dependency.remoteObject = "module load X11";
        dependencies.put( X11_DEPENDENCY, dependency );
    }

    private ArrayList< String > jobScriptCommands()
    {
        ArrayList< String > commands = new ArrayList< String >(  );

        commands.add( (String) dependencies.get( JAVA_DEPENDENCY ).remoteObject );
        commands.add( (String) dependencies.get( X11_DEPENDENCY ).remoteObject );

        /*
        String command = xvfb
                + imageJDirectory
                + File.separator
                + relativeImageJBinaryPath
                + scriptPath;
                */

        return commands;

    }

    public String jobText()
    {
        slurmJobScript.executableCommands = jobScriptCommands();

        return slurmJobScript.jobText();
    }


}
