/*
 =================== DO NOT EDIT THIS FILE ====================
 Generated by Modello 1.0.2 on 2010-01-11 12:09:20,
 any modifications will be overwritten.
 ==============================================================
 */

package org.sonatype.nexus.rest.model;

/**
 * Class NexusRepositoryTypeListResource.
 * 
 * @version $Revision$ $Date$
 */
public class NexusRepositoryTypeListResource
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field provider.
     */
    private String provider;

    /**
     * Field providerRole.
     */
    private String providerRole;

    /**
     * Field format.
     */
    private String format;

    /**
     * Field description.
     */
    private String description;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the description field.
     * 
     * @return String
     */
    public String getDescription()
    {
        return this.description;
    } //-- String getDescription()

    /**
     * Get the format field.
     * 
     * @return String
     */
    public String getFormat()
    {
        return this.format;
    } //-- String getFormat()

    /**
     * Get the provider field.
     * 
     * @return String
     */
    public String getProvider()
    {
        return this.provider;
    } //-- String getProvider()

    /**
     * Get the providerRole field.
     * 
     * @return String
     */
    public String getProviderRole()
    {
        return this.providerRole;
    } //-- String getProviderRole()

    /**
     * Set the description field.
     * 
     * @param description
     */
    public void setDescription( String description )
    {
        this.description = description;
    } //-- void setDescription( String )

    /**
     * Set the format field.
     * 
     * @param format
     */
    public void setFormat( String format )
    {
        this.format = format;
    } //-- void setFormat( String )

    /**
     * Set the provider field.
     * 
     * @param provider
     */
    public void setProvider( String provider )
    {
        this.provider = provider;
    } //-- void setProvider( String )

    /**
     * Set the providerRole field.
     * 
     * @param providerRole
     */
    public void setProviderRole( String providerRole )
    {
        this.providerRole = providerRole;
    } //-- void setProviderRole( String )


}
