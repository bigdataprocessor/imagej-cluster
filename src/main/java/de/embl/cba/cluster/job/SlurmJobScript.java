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
package de.embl.cba.cluster.job;

import de.embl.cba.cluster.JobSettings;
import de.embl.cba.cluster.SSHExecutorService;

import java.util.ArrayList;


/**
 * https://wiki.embl.de/cluster/Env#Queues
 */
public class SlurmJobScript implements JobScript
{
    public static final String DO_NOT_ECHO = " # Do not print";
    private final JobSettings jobSettings;
    public static final String XVFB_ERR_PATH = "XVFB_ERR_PATH";

    private ArrayList< String > executableCommands;

    public SlurmJobScript( ArrayList< String > executableCommands, JobSettings jobSettings )
    {
        this.executableCommands = executableCommands;
        this.jobSettings = jobSettings;
    }

    public String getJobText( SSHExecutorService sshExecutorService, String jobID )
    {
        ArrayList < String > lines = new ArrayList< >(  );

        lines.add( "#!/bin/bash" );
        lines.add( "#SBATCH -e " + sshExecutorService.getJobErrPath( jobID ) );
        lines.add( "#SBATCH -o " + sshExecutorService.getJobOutPath( jobID ) );
        lines.add( "#SBATCH -N 1" );
        lines.add( "#SBATCH -n 1" );
        lines.add( "#SBATCH -c " + jobSettings.numWorkersPerNode );
        lines.add( "#SBATCH --mem " + jobSettings.memoryPerJobInMegaByte );
        lines.add( "#SBATCH -p " + jobSettings.queue );

        int time = jobSettings.timePerJobInMinutes;
        int hours = time / 60;
        int minutes = time % 60;

        lines.add( "#SBATCH -t " + String.format( "%02d", hours ) + ":" + String.format( "%02d", minutes ) + ":00");

        lines.add( "ulimit -c 0" );

        lines.add( sshExecutorService.getJobStartedCommand( jobID ) );

        lines.add( "date" );
        lines.add( "echo \"job started\"" );

        for ( String executableCommand : executableCommands )
        {
            if ( executableCommand.contains( XVFB_ERR_PATH  ) )
            {
                executableCommand = executableCommand.replace(  XVFB_ERR_PATH, sshExecutorService.getJobXvfbErrPath( jobID ) );
            }

            // execution time of the command
            lines.add( "date" );

            // potentially echo the command
            if ( ! executableCommand.contains( DO_NOT_ECHO ) )
            {
//				final String printableCommand = executableCommand.replace( "(", "\\(" ).replace( ")", "\\)" );
//                final String printableCommand = executableCommand.replace( "(", "\\(" ).replace( ")", "\\)" );
                String noSingleQuotes = executableCommand.replace( "'", "SINGLEQUOTE" );
                lines.add( "echo \'" + noSingleQuotes + "\'" );
            }
            else
            {
                executableCommand = executableCommand.replace( DO_NOT_ECHO, "" );
            }

            // add the actual command
            lines.add( executableCommand );
        }

        lines.add( sshExecutorService.getJobFinishedCommand( jobID ) );

        return String.join( "\n", lines );
    }
}
