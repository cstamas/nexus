/*
 =================== DO NOT EDIT THIS FILE ====================
 Generated by Modello 1.0.2 on 2010-01-11 12:09:20,
 any modifications will be overwritten.
 ==============================================================
 */

package org.sonatype.nexus.rest.model;

/**
 * Class ErrorReportRequest.
 * 
 * @version $Revision$ $Date$
 */
public class ErrorReportRequest
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field data.
     */
    private ErrorReportRequestDTO data;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the data field.
     * 
     * @return ErrorReportRequestDTO
     */
    public ErrorReportRequestDTO getData()
    {
        return this.data;
    } //-- ErrorReportRequestDTO getData()

    /**
     * Set the data field.
     * 
     * @param data
     */
    public void setData( ErrorReportRequestDTO data )
    {
        this.data = data;
    } //-- void setData( ErrorReportRequestDTO )


}
