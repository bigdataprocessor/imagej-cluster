#@ File(label="Image path", value="/Users/tischer/Documents/fiji-slurm/src/test/resources/horizontal-line.tif") IMAGE_FILE
#@ Integer(label="Angle [degrees]", value=50) ANGLE_IN_DEGREES
#@ File(label="Output directory", style="directory", value="/Users/tischer/Documents/fiji-slurm/src/test/resources") OUTPUT_DIRECTORY

/* 

On a mac call me like this from commands line:

/Applications/Fiji.app/Contents/MacOS/ImageJ-macosx --ij2 --headless --run /Users/tischer/Documents/fiji-slurm/src/test/resources/ij2plugin-rotate-image.groovy "IMAGE_FILE='/Users/tischer/Documents/fiji-slurm/src/test/resources/horizontal-line.tif',ANGLE_IN_DEGREES=50,OUTPUT_DIRECTORY='/Users/tischer/Documents/fiji-slurm/src/test/resources'"

*/



import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import java.io.File;
import ij.measure.ResultsTable;

final Integer MIN_VOXELS = 10;

// Load data
//
ImagePlus imp = IJ.openImage( IMAGE_FILE.getAbsolutePath() );
//imp.show();

// Apply
//
IJ.run(imp, "Rotate... ", "angle=" + ANGLE_IN_DEGREES + " grid=1 interpolation=Bilinear");

// Show results
//
// imp.show()

// Save results
//
String savingPath;
savingPath = "" + OUTPUT_DIRECTORY + File.separator + imp.getTitle() + "--rotated" + ANGLE_IN_DEGREES + "degrees.tif";
FileSaver fileSaver = new FileSaver( imp );
fileSaver.saveAsTiff( savingPath );

// Quit ImageJ
//
// IJ.run("Quit");