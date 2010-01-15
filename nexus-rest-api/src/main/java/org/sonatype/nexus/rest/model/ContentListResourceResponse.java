/*
 =================== DO NOT EDIT THIS FILE ====================
 Generated by Modello 1.0.2 on 2010-01-11 12:09:20,
 any modifications will be overwritten.
 ==============================================================
 */

package org.sonatype.nexus.rest.model;

/**
 * Class ContentListResourceResponse.
 * 
 * @version $Revision$ $Date$
 */
public class ContentListResourceResponse
    extends NexusResponse
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field data.
     */
    private java.util.List<ContentListResource> data;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addData.
     * 
     * @param contentListResource
     */
    public void addData( ContentListResource contentListResource )
    {
        if ( !(contentListResource instanceof ContentListResource) )
        {
            throw new ClassCastException( "ContentListResourceResponse.addData(contentListResource) parameter must be instanceof " + ContentListResource.class.getName() );
        }
        getData().add( contentListResource );
    } //-- void addData( ContentListResource )

    /**
     * Method getData.
     * 
     * @return List
     */
    public java.util.List<ContentListResource> getData()
    {
        if ( this.data == null )
        {
            this.data = new java.util.ArrayList<ContentListResource>();
        }

        return this.data;
    } //-- java.util.List<ContentListResource> getData()

    /**
     * Method removeData.
     * 
     * @param contentListResource
     */
    public void removeData( ContentListResource contentListResource )
    {
        if ( !(contentListResource instanceof ContentListResource) )
        {
            throw new ClassCastException( "ContentListResourceResponse.removeData(contentListResource) parameter must be instanceof " + ContentListResource.class.getName() );
        }
        getData().remove( contentListResource );
    } //-- void removeData( ContentListResource )

    /**
     * Set the data field.
     * 
     * @param data
     */
    public void setData( java.util.List<ContentListResource> data )
    {
        this.data = data;
    } //-- void setData( java.util.List )


}
