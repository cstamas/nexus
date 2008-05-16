/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.creator;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.ArtifactIndexingContext;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * @plexus.component role-hint="jarContents-uniqueArtifactInfo"
 */
public class JarFileContentsIndexCreator
    extends AbstractIndexCreator
{
  
    public void populateArtifactInfo( ArtifactIndexingContext context ) throws IOException 
    {
        ArtifactContext artifactContext = context.getArtifactContext();
        
        ArtifactInfo ai = artifactContext.getArtifactInfo();

        File artifactFile = artifactContext.getArtifact();

        if ( artifactFile != null && artifactFile.exists() && artifactFile.getName().endsWith( ".jar" ) )
        {
            ai.classNames = getClasses( artifactFile );
        }
    }
  
    public void updateDocument( ArtifactIndexingContext context, Document doc )
    {
        ArtifactInfo ai = context.getArtifactContext().getArtifactInfo();

        if ( ai.classNames != null )
        {
            doc.add( new Field( ArtifactInfo.NAMES, ai.classNames, Field.Store.COMPRESS, Field.Index.TOKENIZED ) );
        }
    }

    public boolean updateArtifactInfo( IndexingContext ctx, Document doc, ArtifactInfo artifactInfo )
    {
        String names = doc.get( ArtifactInfo.NAMES );

        if ( names != null )
        {
            artifactInfo.classNames = names;

            return true;
        }

        return false;
    }

    private String getClasses( File f )
        throws IOException
    {
        int totalClasses = 0;

        ZipFile jar = null;

        try
        {
            jar = new ZipFile( f );

            StringBuilder sb = new StringBuilder();

            @SuppressWarnings( "unchecked" )
            Enumeration en = jar.entries();
            while ( en.hasMoreElements() )
            {
                ZipEntry e = (ZipEntry) en.nextElement();

                String name = e.getName();

                if ( name.endsWith( ".class" ) )
                {
                    totalClasses++;

                    // TODO verify if class is public or protected
                    // TODO skip all inner classes for now

                    int i = name.lastIndexOf( "$" );

                    if ( i == -1 )
                    {
                        sb.append( name.substring( 0, name.length() - 6 ) ).append( "\n" );
                    }
                }
            }

            return sb.toString();
        }
        finally
        {
            if ( jar != null )
            {
                try
                {
                    jar.close();
                }
                catch ( Exception e )
                {
                    getLogger().error( "Could not close jar file properly.", e );
                }
            }
        }
    }

}
