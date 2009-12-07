package org.sonatype.nexus.plugins;

/**
 * Plugin static resource model, upon which the static resource and PluginResourceBundle will be built.
 * 
 * @author cstamas
 */
public class PluginStaticResourceModel
{
    private final String resourcePath;

    private final String publishedPath;

    private final String contentType;

    public PluginStaticResourceModel( String resourcePath, String publishedPath, String contentType )
    {
        this.resourcePath = resourcePath;

        this.publishedPath = publishedPath;

        this.contentType = contentType;
    }

    public String getResourcePath()
    {
        return resourcePath;
    }

    public String getPublishedPath()
    {
        return publishedPath;
    }

    public String getContentType()
    {
        return contentType;
    }
}
