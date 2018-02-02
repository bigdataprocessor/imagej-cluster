package de.embl.cba.cluster.job;

import de.embl.cba.cluster.SSHExecutorService;

public interface JobScript
{
    String getJobText( SSHExecutorService SSHExecutorService );
}
