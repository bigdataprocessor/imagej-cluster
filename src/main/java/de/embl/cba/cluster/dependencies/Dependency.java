package de.embl.cba.cluster.dependencies;

public class Dependency
{

    public String key;
    public DependencyType localType;
    public DependencyType remoteType;
    public Object localObject;
    public Object remoteObject;


    public void setLocalSource( Object localObject )
    {
        this.localObject = localObject;
    }

}
