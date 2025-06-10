/*-
 * #%L
 * Submitting and monitoring ImageJ jobs on a computer cluster
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
