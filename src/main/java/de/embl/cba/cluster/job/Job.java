package de.embl.cba.cluster.job;

import de.embl.cba.cluster.SSHExecutorService;

public interface Job
{
    String getJobText( SSHExecutorService SSHExecutorService );
}
