package de.embl.cba.cluster.plugins;

import ij.IJ;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>Institute Specific>EMBL>Log Text Test" )
public class LogTextPlugin implements Command
{
    @Parameter( label = "Please enter some text", required = true )
    public String text = "Hello World!";

    public void run()
    {
        IJ.log( "You entered: " + text );
    }


    // /Applications/Fiji.app/Contents/MacOS/ImageJ-macosx --ij2 --run "Log Text Test" "text='aaa'"
    // /Applications/Fiji.app/Contents/MacOS/ImageJ-macosx --run "Log Text Test" "text='aaa'"


    // /Applications/Fiji.app/Contents/MacOS/ImageJ-macosx --run "Apply Classifier" "quitAfterRun='false'"
    //
    // ,inputImagePath='/Users/tischer/Documents/fiji-plugin-deepSegmentation/src/test/resources/image-sequence/.*--W00016--P00003--.*',classifierPath='/Users/tischer/Documents/fiji-plugin-deepSegmentation/src/test/resources/transmission-cells-3d.classifier',outputModality='Show all probabilities in one image',outputDirectory='/Users/tischer/Documents/fiji-plugin-deepSegmentation/src/test/resources'"

}