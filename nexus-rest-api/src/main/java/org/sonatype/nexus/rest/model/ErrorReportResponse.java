/*
 =================== DO NOT EDIT THIS FILE ====================
 Generated by Modello 1.0.2 on 2010-01-11 12:09:20,
 any modifications will be overwritten.
 ==============================================================
 */

package org.sonatype.nexus.rest.model;

/**
 * Class ErrorReportResponse.
 * 
 * @version $Revision$ $Date$
 */
public class ErrorReportResponse
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field data.
     */
    private ErrorReportResponseDTO data;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the data field.
     * 
     * @return ErrorReportResponseDTO
     */
    public ErrorReportResponseDTO getData()
    {
        return this.data;
    } //-- ErrorReportResponseDTO getData()

    /**
     * Set the data field.
     * 
     * @param data
     */
    public void setData( ErrorReportResponseDTO data )
    {
        this.data = data;
    } //-- void setData( ErrorReportResponseDTO )


}
