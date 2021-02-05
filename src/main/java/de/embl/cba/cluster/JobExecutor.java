package de.embl.cba.cluster;

public class JobExecutor
{
	public enum ScriptType
	{
		SlurmJob,
		LinuxShell
	}

	public String hostName;
	public ScriptType scriptType;

}
