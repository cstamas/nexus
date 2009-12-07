package org.sonatype.nexus.plugins;

/**
 * Model describing what repository type did plugin contribute to Nexus core.
 * 
 * @author cstamas
 */
public class PluginRepositoryType
{
    private String componentContract;

    private String pathPrefix;

    public PluginRepositoryType( String componentContract, String pathPrefix )
    {
        this.componentContract = componentContract;

        this.pathPrefix = pathPrefix;
    }

    public String getComponentContract()
    {
        return componentContract;
    }

    public void setComponentContract( String componentContract )
    {
        this.componentContract = componentContract;
    }

    public String getPathPrefix()
    {
        return pathPrefix;
    }

    public void setPathPrefix( String pathPrefix )
    {
        this.pathPrefix = pathPrefix;
    }
}
