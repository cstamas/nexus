/*
 =================== DO NOT EDIT THIS FILE ====================
 Generated by Modello 1.0.2 on 2010-01-11 12:09:20,
 any modifications will be overwritten.
 ==============================================================
 */

package org.sonatype.nexus.rest.model;

/**
 * Class RestApiSettings.
 * 
 * @version $Revision$ $Date$
 */
public class RestApiSettings
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field forceBaseUrl.
     */
    private boolean forceBaseUrl = false;

    /**
     * Field baseUrl.
     */
    private String baseUrl;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the baseUrl field.
     * 
     * @return String
     */
    public String getBaseUrl()
    {
        return this.baseUrl;
    } //-- String getBaseUrl()

    /**
     * Get the forceBaseUrl field.
     * 
     * @return boolean
     */
    public boolean isForceBaseUrl()
    {
        return this.forceBaseUrl;
    } //-- boolean isForceBaseUrl()

    /**
     * Set the baseUrl field.
     * 
     * @param baseUrl
     */
    public void setBaseUrl( String baseUrl )
    {
        this.baseUrl = baseUrl;
    } //-- void setBaseUrl( String )

    /**
     * Set the forceBaseUrl field.
     * 
     * @param forceBaseUrl
     */
    public void setForceBaseUrl( boolean forceBaseUrl )
    {
        this.forceBaseUrl = forceBaseUrl;
    } //-- void setForceBaseUrl( boolean )


}
