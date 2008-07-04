/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.maven;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ResourceStoreRequest;

public class ArtifactStoreRequest
    extends ResourceStoreRequest
{
    private String groupId;

    private String artifactId;

    private String version;

    private String packaging;

    private String classifier;

    public ArtifactStoreRequest( boolean localOnly, String repositoryId, String repositoryGroupId, String g, String a,
        String v, String p, String c )
    {
        super( "GAV", localOnly, repositoryId, repositoryGroupId );

        if ( StringUtils.isEmpty( g ) || StringUtils.isEmpty( a ) || StringUtils.isEmpty( v ) )
        {
            throw new IllegalArgumentException( "None of the GAV dimensions can be null or empty!" );
        }

        setGroupId( g );

        setArtifactId( a );

        setVersion( v );

        if ( !StringUtils.isEmpty( p ) )
        {
            setPackaging( p );
        }
        else
        {
            setPackaging( "jar" );
        }

        if ( !StringUtils.isEmpty( c ) )
        {
            setClassifier( c );
        }
        else
        {
            setClassifier( null );
        }
    }

    public ArtifactStoreRequest( String g, String a, String v, String p, String c )
    {
        this( false, null, null, g, a, v, p, c );
    }

    public ArtifactStoreRequest( String g, String a, String v )
    {
        this( false, null, null, g, a, v, null, null );
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getPackaging()
    {
        return packaging;
    }

    public void setPackaging( String packaging )
    {
        this.packaging = packaging;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer( getGroupId() );
        sb.append( ":" );
        sb.append( getArtifactId() );
        sb.append( ":" );
        sb.append( getPackaging() );
        sb.append( ":" );
        sb.append( getClassifier() );
        sb.append( ":" );
        sb.append( getVersion() );

        return sb.toString();
    }

}
