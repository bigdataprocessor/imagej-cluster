package de.embl.cba.cluster.job;

import de.embl.cba.cluster.SlurmExecutorService;

public interface SlurmJob
{
    String getJobText( SlurmExecutorService slurmExecutorService );
}
