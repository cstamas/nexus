/*
 =================== DO NOT EDIT THIS FILE ====================
 Generated by Modello 1.0.2 on 2010-01-11 12:09:20,
 any modifications will be overwritten.
 ==============================================================
 */

package org.sonatype.nexus.rest.model;

/**
 * Class RepositoryGroupMemberRepository.
 * 
 * @version $Revision$ $Date$
 */
public class RepositoryGroupMemberRepository
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field id.
     */
    private String id;

    /**
     * Field name.
     */
    private String name;

    /**
     * Field resourceURI.
     */
    private String resourceURI;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the id field.
     * 
     * @return String
     */
    public String getId()
    {
        return this.id;
    } //-- String getId()

    /**
     * Get the name field.
     * 
     * @return String
     */
    public String getName()
    {
        return this.name;
    } //-- String getName()

    /**
     * Get the resourceURI field.
     * 
     * @return String
     */
    public String getResourceURI()
    {
        return this.resourceURI;
    } //-- String getResourceURI()

    /**
     * Set the id field.
     * 
     * @param id
     */
    public void setId( String id )
    {
        this.id = id;
    } //-- void setId( String )

    /**
     * Set the name field.
     * 
     * @param name
     */
    public void setName( String name )
    {
        this.name = name;
    } //-- void setName( String )

    /**
     * Set the resourceURI field.
     * 
     * @param resourceURI
     */
    public void setResourceURI( String resourceURI )
    {
        this.resourceURI = resourceURI;
    } //-- void setResourceURI( String )


}
