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
import de.embl.cba.cluster.*;
import de.embl.cba.cluster.develop.PrintTextCommand;
import de.embl.cba.cluster.log.IJLazySwingLogger;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.TextWidget;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static de.embl.cba.cluster.JobSubmitter.IMAGEJ_EXECTUABLE_ALMF_CLUSTER_HEADLESS;

/* Simple test, just printing text onto command line */

//@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>ClusterTest>Print Text On Cluster" )
public class PrintTextOnCluster implements Command
{
    @Parameter
    public LogService logService;

    @Parameter( label = "Username" )
    private String username;

    @Parameter( label = "Password", style = TextWidget.PASSWORD_STYLE, persist = false )
    private String password;

    @Parameter (label = "Job directory", style = "directory" )
    public File jobDirectory;

    @Parameter (label = "Some text"  )
    public String inputText = "Hello world!";

    @Parameter (label = "Number of jobs" )
    public int numJobs = 4;

    @Parameter (label = "Memory [MB]"  )
    public int memoryMB = 2000;

    @Parameter (label = "Number of threads per job" )
    public int numWorkers = 1;

    @Parameter (label = "Time per job in minutes" )
    public int timePerJobInMinutes = 3;

    IJLazySwingLogger logger = new IJLazySwingLogger();

    public void run()
    {
        logger.setLogService( logService );

        ArrayList< JobFuture > jobFutures = submitJobsOnSlurm(
                IMAGEJ_EXECTUABLE_ALMF_CLUSTER_HEADLESS,
                jobDirectory.toPath() );

        JobMonitor jobMonitor = new JobMonitor( logger );

        jobMonitor.monitorJobProgress(
                jobFutures,
                3,
                0 );
    }

    private ArrayList< JobFuture > submitJobsOnSlurm( String imageJ, Path jobDirectory )
    {
        JobSubmitter commandsSubmitter = getImageJJobSubmitter( imageJ, jobDirectory );

        JobSettings jobSettings = getJobSettings();

        ArrayList< JobFuture > jobFutures = new ArrayList<>( );

        for ( int i = 0; i < numJobs; ++i )
        {
            commandsSubmitter.clearCommands();
            setCommandAndParameterStrings( commandsSubmitter, inputText );
            jobFutures.add( commandsSubmitter.submitJob( jobSettings ) );

            try
            {
                Thread.sleep( 5000 );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }

        return jobFutures;
    }

    private JobSettings getJobSettings()
    {
        JobSettings jobSettings = new JobSettings();
        jobSettings.queue = JobSettings.DEFAULT_QUEUE;
        jobSettings.numWorkersPerNode = numWorkers;
        jobSettings.timePerJobInMinutes = timePerJobInMinutes;
        jobSettings.memoryPerJobInMegaByte = memoryMB;
        return jobSettings;
    }

    private JobSubmitter getImageJJobSubmitter( String imageJ, Path jobDirectory )
    {
        return new JobSubmitter(
                new JobExecutor(),
                PathMapper.asEMBLClusterMounted( jobDirectory.toString() ),
                imageJ,
                username,
                password );
    }

    private void setCommandAndParameterStrings( JobSubmitter commandsSubmitter, String text )
    {
        Map< String, Object > parameters = new HashMap<>();
        parameters.clear();
        parameters.put( PrintTextCommand.INPUT_TEXT, inputText );
        parameters.put( PrintTextCommand.QUIT_AFTER_RUN, true );
        commandsSubmitter.addIJCommandWithParameters( PrintTextCommand.PLUGIN_NAME , parameters );
    }

    public static void main ( String[] args ) throws IOException
    {
        ImageJ ij = new ImageJ();
        ij.ui().showUI();
        ij.command().run( PrintTextOnCluster.class, true );
    }
}
