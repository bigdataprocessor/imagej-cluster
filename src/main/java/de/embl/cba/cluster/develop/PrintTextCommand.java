/*-
 * #%L
 * Submitting and monitoring ImageJ jobs on a computer cluster
 * %%
 * Copyright (C) 2018 - 2020 EMBL
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
import ij.CompositeImage;
import net.imagej.legacy.IJ1Helper;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import net.imagej.legacy.LegacyService;

import java.awt.*;
import java.awt.image.IndexColorModel;

@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>Print text")
public class PrintTextCommand implements Command
{
    public static final String PLUGIN_NAME = "Print text";

    @Parameter
    private LegacyService legacyService;

    @Parameter
    public LogService logService;

    @Parameter( label = "Some text" )
    public String inputText;
    public static final String INPUT_TEXT = "inputText";

    @Parameter( label = "Quit after running" )
    public boolean quitAfterRun = false;
    public static final String QUIT_AFTER_RUN = "quitAfterRun";

    public void run()
    {
        logService.info( "# PrintTextCommand" );
        logService.info( "Text: " + inputText );

        final IJ1Helper helper = legacyService.getIJ1Helper();
        logService.info( "isRMIEnabled: " + helper.isRMIEnabled() );

        if ( quitAfterRun ) Commands.quitImageJ( logService );

        return;
    }

}