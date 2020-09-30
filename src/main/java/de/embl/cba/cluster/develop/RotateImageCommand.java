package de.embl.cba.cluster.develop;

import de.embl.cba.cluster.Commands;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import net.imagej.DatasetService;
import net.imagej.ops.OpService;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;

import java.io.File;

@Plugin(type = Command.class) // , menuPath = "Plugins>Registration>Development>"+PLUGIN_NAME
public class RotateImageCommand implements Command
{
    public static final String PLUGIN_NAME = "Rotate Image";

    @Parameter
    public UIService uiService;

    @Parameter
    public DatasetService datasetService;

    @Parameter
    public LogService logService;

    @Parameter
    public ThreadService threadService;

    @Parameter
    public OpService opService;

    @Parameter
    public StatusService statusService;

    @Parameter( label = "Rotation angle [degrees]" )
    public double rotationAngle;
    public static final String ROTATION_ANGLE = "rotationAngle";

    @Parameter( label = "Input image", style = FileWidget.OPEN_STYLE, required = false )
    public File inputImagePath;
    public static final String INPUT_IMAGE_PATH = "inputImagePath";

    @Parameter( label = "Output path", style = FileWidget.SAVE_STYLE, required = false )
    public File outputImagePath;
    public static final String OUTPUT_IMAGE_PATH = "outputImagePath";

    @Parameter( label = "Quit after running", required = true )
    public boolean quitAfterRun = false;
    public static final String QUIT_AFTER_RUN = "quitAfterRun";

    ImagePlus inputImage;

    public void run()
    {
        logService.info( "# " + PLUGIN_NAME );

        getData();
        runAlgorithm();
        putData();

        if ( quitAfterRun ) Commands.quitImageJ( logService );

        logService.info( "Done in command." );

        return;
    }


    private void runAlgorithm()
    {
        logService.info( "Rotating by [degrees]: " + rotationAngle );
        IJ.run( inputImage, "Rotate... ", "angle=" + rotationAngle + " grid=1 interpolation=Bilinear" );
    }

    private void putData()
    {
        if ( outputImagePath == null )
        {
            //
        }
        else
        {
            logService.info( "Saving: " + outputImagePath.getAbsolutePath() );
            FileSaver fileSaver = new FileSaver( inputImage );
            fileSaver.saveAsTiff( outputImagePath.getAbsolutePath() );
        }
    }

    private void getData()
    {
        if ( inputImagePath == null )
        {
            inputImage = IJ.getImage();
        }
        else
        {
            logService.info( "Loading: " + inputImagePath );

            inputImage = IJ.openImage( inputImagePath.getAbsolutePath() );

            if ( inputImage == null )
            {
                logService.error( "Loading of " + inputImagePath + "failed!" );
                if ( quitAfterRun ) Commands.quitImageJ( logService );
            }
        }
    }

}