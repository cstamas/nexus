/*
 =================== DO NOT EDIT THIS FILE ====================
 Generated by Modello 1.0.2 on 2010-01-11 12:09:20,
 any modifications will be overwritten.
 ==============================================================
 */

package org.sonatype.nexus.rest.model;

/**
 * Class LogConfigResourceResponse.
 * 
 * @version $Revision$ $Date$
 */
public class LogConfigResourceResponse
    extends NexusResponse
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field data.
     */
    private LogConfigResource data;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the data field.
     * 
     * @return LogConfigResource
     */
    public LogConfigResource getData()
    {
        return this.data;
    } //-- LogConfigResource getData()

    /**
     * Set the data field.
     * 
     * @param data
     */
    public void setData( LogConfigResource data )
    {
        this.data = data;
    } //-- void setData( LogConfigResource )


}
