package de.embl.cba.cluster;

import de.embl.cba.log.IJLazySwingLogger;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.widget.TextWidget;

import java.io.File;
import java.util.ArrayList;

public abstract class AbstractClusterSubmitterCommand implements Command
{
    @Parameter( label = "Executable", required = false)
    protected File executable = new File( JobSubmitter.IMAGEJ_EXECTUABLE_ALMF_CLUSTER_HEADLESS );

    @Parameter( label = "Job directory", required = false)
    protected File jobDirectory = new File( "/g/cba/cluster/" );

    @Parameter( label = "Username" )
    protected String userName = "tischer";

    @Parameter( label = "Password", style = TextWidget.PASSWORD_STYLE, persist = false )
    protected String password;

    @Parameter( label = "Number of CPUs per job" )
    protected int numWorkers = 1;

    @Parameter( label = "Job status monitoring interval [s]" )
    protected int jobStatusMonitoringInterval = 60;

    @Parameter( label = "Maximum number of failed job resubmissions" )
    protected int maxNumResubmissions = 0;
    private JobSubmitter jobSubmitter;
    private SlurmJobMonitor jobMonitor;
    private ArrayList< JobFuture > jobFutures;


    /**
     * createJobSubmitter();
     * jobFutures = submitJobsOnSlurm( ... );
     * monitorJobs( jobFutures );
     */

    private void monitorJobs( ArrayList< JobFuture > jobFutures )
    {
        jobMonitor = new SlurmJobMonitor( new IJLazySwingLogger() );
        jobMonitor.monitorJobProgress( jobFutures, jobStatusMonitoringInterval, maxNumResubmissions );
    }

    private void createJobSubmitter()
    {
        jobSubmitter = new JobSubmitter(
                JobSubmitter.EXECUTION_SYSTEM_EMBL_SLURM,
                new File ( jobDirectory, userName ).toString(),
                executable.toString(),
                userName,
                password );
    }
}
