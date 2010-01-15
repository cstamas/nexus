/*
 =================== DO NOT EDIT THIS FILE ====================
 Generated by Modello 1.0.2 on 2010-01-11 12:09:20,
 any modifications will be overwritten.
 ==============================================================
 */

package org.sonatype.nexus.rest.model;

/**
 * Class MirrorResourceListRequest.
 * 
 * @version $Revision$ $Date$
 */
public class MirrorResourceListRequest
    extends NexusResponse
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field data.
     */
    private java.util.List<MirrorResource> data;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addData.
     * 
     * @param mirrorResource
     */
    public void addData( MirrorResource mirrorResource )
    {
        if ( !(mirrorResource instanceof MirrorResource) )
        {
            throw new ClassCastException( "MirrorResourceListRequest.addData(mirrorResource) parameter must be instanceof " + MirrorResource.class.getName() );
        }
        getData().add( mirrorResource );
    } //-- void addData( MirrorResource )

    /**
     * Method getData.
     * 
     * @return List
     */
    public java.util.List<MirrorResource> getData()
    {
        if ( this.data == null )
        {
            this.data = new java.util.ArrayList<MirrorResource>();
        }

        return this.data;
    } //-- java.util.List<MirrorResource> getData()

    /**
     * Method removeData.
     * 
     * @param mirrorResource
     */
    public void removeData( MirrorResource mirrorResource )
    {
        if ( !(mirrorResource instanceof MirrorResource) )
        {
            throw new ClassCastException( "MirrorResourceListRequest.removeData(mirrorResource) parameter must be instanceof " + MirrorResource.class.getName() );
        }
        getData().remove( mirrorResource );
    } //-- void removeData( MirrorResource )

    /**
     * Set the data field.
     * 
     * @param data
     */
    public void setData( java.util.List<MirrorResource> data )
    {
        this.data = data;
    } //-- void setData( java.util.List )


}
