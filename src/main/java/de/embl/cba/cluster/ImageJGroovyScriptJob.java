package de.embl.cba.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageJGroovyScriptJob
{
    public LinkedHashMap< String, Dependency > dependencies;
    public static final String JAVA_DEPENDENCY = "Java";
    public static final String X11_DEPENDENCY = "X11";
    public static final String XVFB_DEPENDENCY = "Xvfb";
    public static final String IMAGEJ_DEPENDENCY = "ImageJ";
    public static final String REMOTE_JOB_DIRECTORY_DEPENDENCY = "JobDir";
    public static final String GROOVY_SCRIPT_DEPENDENCY = "GroovyScript";

    private String groovyScriptName = "script.groovy";

    private SlurmJobScript slurmJobScript;

    private String jobRemoteFilename = "job";

    public ImageJGroovyScriptJob(  )
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

        addRemoteJobDirectoryDependency();
        addJavaDependency();
        addX11Dependency();
        addXVFBDependency();
        addGroovyScriptDependency();
        addImageJDependency();
        // XVFB
        // IMAGEJ

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

    private void addXVFBDependency()
    {
        Dependency dependency = new Dependency();
        dependency.remoteType = DependencyType.Command;
        dependency.remoteObject = "xvfb-run -a";
        dependencies.put( XVFB_DEPENDENCY, dependency );
    }

    private void addRemoteJobDirectoryDependency()
    {
        Dependency dependency = new Dependency();
        dependency.remoteType = DependencyType.Directory;
        dependency.remoteObject = null;
        dependencies.put( REMOTE_JOB_DIRECTORY_DEPENDENCY, dependency );
    }

    private void addGroovyScriptDependency()
    {
        Dependency dependency = new Dependency();
        dependency.localType = DependencyType.Text;
        dependency.localObject = null;
        dependency.remoteType = DependencyType.Path;
        dependency.remoteObject = null;
        dependencies.put( GROOVY_SCRIPT_DEPENDENCY, dependency );
    }

    private void addImageJDependency()
    {
        Dependency dependency = new Dependency();
        dependency.localType = null;
        dependency.localObject = null;
        dependency.remoteType = DependencyType.Path;
        dependency.remoteObject = "/g/almf/software/Fiji/ImageJ-linux64";
        dependencies.put( IMAGEJ_DEPENDENCY, dependency );
    }

    private ArrayList< String > jobScriptCommands()
    {
        ArrayList< String > commands = new ArrayList< String >(  );

        commands.add( (String) dependencies.get( JAVA_DEPENDENCY ).remoteObject );
        commands.add( (String) dependencies.get( X11_DEPENDENCY ).remoteObject );

        String command = dependencies.get( XVFB_DEPENDENCY ).remoteObject
                + " " + dependencies.get( IMAGEJ_DEPENDENCY ).remoteObject
                + " " + dependencies.get( GROOVY_SCRIPT_DEPENDENCY ).remoteObject;

        commands.add( command );

        return commands;

    }

    public void manageDependencies( SlurmExecutorService executorService )
    {
        manageJobDirectoryDependency( executorService.getRemoteJobDirectory() );
        manageGroovyScriptDependency( executorService.getRemoteJobDirectory() );
    }

    private void manageGroovyScriptDependency( String remoteJobDirectory )
    {
        Utils.saveTextAsFile( getLocalDependencyGroovyScriptText(), getGroovyScriptName(), Utils.localMounting( remoteJobDirectory ) );
        String remoteScriptPath = remoteJobDirectory + "/" + getGroovyScriptName();
        setRemoteDependencyGroovyScriptPath( remoteScriptPath );
    }

    private void manageJobDirectoryDependency( String remoteJobDirectory )
    {
        setRemoteDependencyJobDirectory( remoteJobDirectory );
    }

    public String jobText()
    {
        slurmJobScript.executableCommands = jobScriptCommands();

        slurmJobScript.jobDirectory = (String) dependencies.get( REMOTE_JOB_DIRECTORY_DEPENDENCY ).remoteObject;

        slurmJobScript.jobRemoteFilename = jobRemoteFilename;

        return slurmJobScript.jobText();
    }

    private void setRemoteDependencyJobDirectory( String directory )
    {
        Dependency dependency = dependencies.get( REMOTE_JOB_DIRECTORY_DEPENDENCY );
        dependency.remoteObject = directory;
    }

    public void setLocalDependencyGroovyScriptNameAndText( String name, String text )
    {
        Dependency dependency = dependencies.get( GROOVY_SCRIPT_DEPENDENCY );
        Map< String, String > map = new HashMap<String, String>(  );
        map.put("name", name);
        map.put("text", text);
        dependency.localObject = map;
    }

    public String getGroovyScriptName( )
    {
        return groovyScriptName;
    }

    private String getLocalDependencyGroovyScriptText( )
    {
        Dependency dependency = dependencies.get( GROOVY_SCRIPT_DEPENDENCY );
        String text = ((Map<String,String>) dependency.localObject).get("text");
        return text;
    }

    private void setRemoteDependencyGroovyScriptPath( String path )
    {
        Dependency dependency = dependencies.get( GROOVY_SCRIPT_DEPENDENCY );
        dependency.remoteObject = path;
    }

    public void setSubmissionFilename( String submissionName )
    {
        this.jobRemoteFilename = submissionName;
    }

}
