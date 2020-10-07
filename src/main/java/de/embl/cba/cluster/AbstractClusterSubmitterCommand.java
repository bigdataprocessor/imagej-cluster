package de.embl.cba.cluster;

import de.embl.cba.log.IJLazySwingLogger;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.widget.TextWidget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractClusterSubmitterCommand implements Command
{
    @Parameter( label = "Executable", required = false)
    protected String executable = JobSubmitter.IMAGEJ_EXECTUABLE_ALMF_CLUSTER_HEADLESS_MACRO;

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

    protected void createJobSubmitter( String executableWithOptions )
    {
        jobSubmitter = new JobSubmitter(
                JobSubmitter.EXECUTION_SYSTEM_EMBL_SLURM,
                new File ( jobDirectory, userName ).toString(),
                executableWithOptions,
                userName,
                password );
    }
}
