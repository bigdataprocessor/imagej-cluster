package de.embl.cba.cluster.job;

import de.embl.cba.cluster.*;
import de.embl.cba.cluster.dependencies.Dependency;
import de.embl.cba.cluster.dependencies.DependencyType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageJGroovyScriptSlurmJob
{
    public LinkedHashMap< String, Dependency > dependencies;
    public static final String JAVA = "Java";
    public static final String X11 = "X11";
    public static final String XVFB = "Xvfb";
    public static final String IMAGEJ = "ImageJ";
    public static final String REMOTE_JOB_DIRECTORY_DEPENDENCY = "JobDir";
    public static final String GROOVY_SCRIPT = "GroovyScript";

    // Groovy script dependencies / parameters
    // TODO: how to set them properly?
    public static final String INPUT_IMAGE = "IMAGE_FILE";
    public static final String OUTPUT_DIRECTORY = "OUTPUT_DIRECTORY";

    private String groovyScriptName = "script.groovy";
    private Map< String, String > groovyScriptParameters = new HashMap< String, String >(  );

    private SlurmJobScript slurmJobScript;

    private String jobRemoteFilename = "job";

    public ImageJGroovyScriptSlurmJob(  )
    {
        configureJobScript();
        addDependencies();
    }

    private void configureJobScript()
    {
        slurmJobScript = new SlurmJobScript( null, 16000, 4, SlurmQueue.DEFAULT_QUEUE);
        slurmJobScript.queue = SlurmQueue.DEFAULT_QUEUE;
        slurmJobScript.memoryPerJobInMegaByte = 10000;
        slurmJobScript.numWorkersPerNode = 4;
    }

    private void addDependencies()
    {
        this.dependencies = new LinkedHashMap< String, Dependency >(  );

        addRemoteJobDirectoryDependency();
        addJavaDependency();
        addX11Dependency();
        addXVFBDependency();
        addImageJDependency();
        addInputImageDependency();
        addOutputDirectoryDependency();

        addGroovyScriptDependency();

    }

    public void manageDependencies( SSHExecutorService executorService ) throws IOException
    {
        manageJobDirectoryDependency( executorService.getJobDirectory() );
        manageGroovyScriptDependency( executorService.getJobDirectory() );
        manageInputImageDependency( executorService.getJobDirectory() );
        manageOutputDirectoryDependency( executorService.getJobDirectory() );
    }

    public void addGroovyScriptParameter( String key, String value )
    {
        groovyScriptParameters.put( key, value );
    }

    private void addJavaDependency()
    {
        Dependency dependency = new Dependency();
        dependency.remoteType = DependencyType.SlurmModule;
        dependency.remoteObject = "module load Java";
        dependencies.put( JAVA, dependency );
    }

    private void addX11Dependency()
    {
        Dependency dependency = new Dependency();
        dependency.remoteType = DependencyType.SlurmModule;
        dependency.remoteObject = "module load X11";
        dependencies.put( X11, dependency );
    }

    private void addXVFBDependency()
    {
        Dependency dependency = new Dependency();
        dependency.remoteType = DependencyType.Command;
        dependency.remoteObject = "xvfb-run -a";
        dependencies.put( XVFB, dependency );
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
        dependencies.put( GROOVY_SCRIPT, dependency );
    }

    private void addImageJDependency()
    {
        Dependency dependency = new Dependency();
        dependency.localType = null;
        dependency.localObject = null;
        dependency.remoteType = DependencyType.Path;
        dependency.remoteObject = "/g/almf/software/Fiji/ImageJ-linux64";
        dependencies.put( IMAGEJ, dependency );
    }

    private void addOutputDirectoryDependency()
    {
        Dependency dependency = new Dependency();
        dependency.remoteType = DependencyType.Directory;
        dependency.remoteObject = null;
        dependencies.put( OUTPUT_DIRECTORY, dependency );
    }

    private void addInputImageDependency()
    {
        Dependency dependency = new Dependency();
        dependency.localType = DependencyType.File;
        dependency.localObject = null;
        dependency.remoteType = DependencyType.Path;
        dependency.remoteObject = null;
        dependencies.put( INPUT_IMAGE, dependency );
    }

    private ArrayList< String > jobScriptCommands()
    {
        ArrayList< String > commands = new ArrayList< String >(  );

        commands.add( (String) dependencies.get( JAVA ).remoteObject );
        commands.add( (String) dependencies.get( X11 ).remoteObject );

        String command = dependencies.get( XVFB ).remoteObject
                + " " + dependencies.get( IMAGEJ ).remoteObject
                + " " + dependencies.get( GROOVY_SCRIPT ).remoteObject;

        String scriptParameters = " \""
                + INPUT_IMAGE + "='" + dependencies.get( INPUT_IMAGE ).remoteObject + "'"
                + "," + "ANGLE_IN_DEGREES=50"
                + "," + OUTPUT_DIRECTORY + "='" + dependencies.get( OUTPUT_DIRECTORY ).remoteObject + "'"
                + "\"";

        commands.add( command + scriptParameters );

        return commands;

    }

    private void manageGroovyScriptDependency( String remoteJobDirectory )
    {
        Utils.saveTextAsFile( getLocalDependencyGroovyScriptText(), groovyScriptName, Utils.localMounting( remoteJobDirectory ) );
        String remoteScriptPath = remoteJobDirectory + "/" + getGroovyScriptName();
        setRemoteDependencyGroovyScriptPath( remoteScriptPath );
    }

    private void manageInputImageDependency( String remoteJobDirectory ) throws IOException
    {
        File localImageFile = (File) dependencies.get( INPUT_IMAGE ).localObject;

        String remoteImagePath = Utils.localMounting( remoteJobDirectory ) + "/" + localImageFile.getName();
        File remoteImageFile = new File( remoteImagePath );

        Utils.copyFileUsingStream( localImageFile, remoteImageFile );

        dependencies.get( INPUT_IMAGE ).remoteObject = remoteImageFile;
    }

    private void manageJobDirectoryDependency( String remoteJobDirectory )
    {
        dependencies.get( REMOTE_JOB_DIRECTORY_DEPENDENCY ).remoteObject = remoteJobDirectory;
    }

    private void manageOutputDirectoryDependency( String remoteJobDirectory )
    {
        dependencies.get( OUTPUT_DIRECTORY ).remoteObject = remoteJobDirectory;
    }

    public void setLocalGroovyScript( File file ) throws IOException
    {
        groovyScriptName = file.getName();
        String scriptText = Utils.readTextFile( file.getParent(), file.getName() );
        dependencies.get( GROOVY_SCRIPT ).localObject = scriptText;
    }

    public void setLocalInputImage( File file )
    {
        dependencies.get( INPUT_IMAGE ).localObject = file;
    }

    public String getGroovyScriptName( )
    {
        return groovyScriptName;
    }

    private String getLocalDependencyGroovyScriptText( )
    {
        Dependency dependency = dependencies.get( GROOVY_SCRIPT );
        String text = (String) dependency.localObject;
        return text;
    }

    private void setRemoteDependencyGroovyScriptPath( String path )
    {
        Dependency dependency = dependencies.get( GROOVY_SCRIPT );
        dependency.remoteObject = path;
    }

    public void setJobFilename( String submissionName )
    {
        this.jobRemoteFilename = submissionName;
    }

}
