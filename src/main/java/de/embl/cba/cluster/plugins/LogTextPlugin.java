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
