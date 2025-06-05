/*-
 * #%L
 * Submitting and monitoring ImageJ jobs on a computer cluster
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package de.embl.cba.cluster;

import de.embl.cba.cluster.log.IJLazySwingLogger;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.widget.TextWidget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractClusterSubmitterCommand implements Command
{
    @Parameter( label = "Executable", required = false)
    protected File executable;

    @Parameter( label = "Job directory", required = false)
    protected File jobDirectory = new File( "/g/cba/cluster/" );

    @Parameter( label = "Username" )
    protected String userName = "tischer";

    @Parameter( label = "Password", style = TextWidget.PASSWORD_STYLE, persist = false )
    protected String password;

    @Parameter( label = "Number of CPUs per job" )
    protected int numWorkers = 8;

    @Parameter( label = "Job status monitoring interval [s]" )
    protected int jobStatusMonitoringInterval = 60;

    @Parameter( label = "Maximum number of failed job resubmissions" )
    protected int maxNumResubmissions = 2;

    protected JobSubmitter jobSubmitter;
    protected JobMonitor jobMonitor;
    protected ArrayList< JobFuture > jobFutures;

    /**
     * createJobSubmitter( exectuable );
     * jobFutures = submitJobsOnSlurm( ... );
     * monitorJobs( jobFutures );
     */

    protected void monitorJobs( List< JobFuture > jobFutures )
    {
        jobMonitor = new JobMonitor( new IJLazySwingLogger() );
        jobMonitor.monitorJobProgress( jobFutures, jobStatusMonitoringInterval, maxNumResubmissions );
    }

    protected void createJobSubmitter( String executableWithOptions, JobExecutor jobExecutor )
    {
        jobSubmitter = new JobSubmitter(
                jobExecutor,
                new File ( jobDirectory, userName ).toString(),
                executableWithOptions,
                userName,
                password );
    }
}
