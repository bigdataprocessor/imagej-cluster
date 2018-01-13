package de.embl.cba.cluster;

import java.io.File;
import java.util.ArrayList;

public class ExecutableImageJGroovyScript implements ExecutableCommands
{

    public final static String XVFB_DEFAULT = "xvfb-run -a";
    public final static String IMAGEJ_DIRECTORY_ALMF_LINUX = "/g/almf/software/Fiji.app/";
    public final static String RELATIVE_IMAGE_BINARY_PATH_ALMF_LINUX = "ImageJ-linux64";

    public String xvfb = XVFB_DEFAULT;
    public String imageJDirectory = IMAGEJ_DIRECTORY_ALMF_LINUX;
    public String relativeImageJBinaryPath = RELATIVE_IMAGE_BINARY_PATH_ALMF_LINUX;
    public String scriptPath = "";
    public ArrayList< String > scriptParameters = new ArrayList< String >(  );


    public ArrayList< String > getExecutableCommands()
    {
        ArrayList< String > commands = new ArrayList< String >(  );
        commands.add("module load Java");
        commands.add("module load X11");

        String command = xvfb
                + imageJDirectory
                + File.separator
                + relativeImageJBinaryPath
                + scriptPath;

        return commands;
    }

}
