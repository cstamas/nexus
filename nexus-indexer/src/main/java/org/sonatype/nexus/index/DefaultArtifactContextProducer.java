/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;
import org.sonatype.nexus.index.locator.GavHelpedLocator;
import org.sonatype.nexus.index.locator.Locator;
import org.sonatype.nexus.index.locator.MetadataLocator;
import org.sonatype.nexus.index.locator.PomLocator;

/**
 * The default implementation of the ArtifactContextProducer.
 * 
 * @author Tamas Cservenak
 * @author Eugene Kuleshov
 * 
 * @plexus.component
 */
public class DefaultArtifactContextProducer
    implements ArtifactContextProducer
{
    private GavHelpedLocator pl = new PomLocator();

    private Locator ml = new MetadataLocator();

    /**
     * Get ArtifactContext for given pom or artifact (jar, war, etc). A file can be
     */
    public ArtifactContext getArtifactContext( IndexingContext context, File file )
    {
        // TODO shouldn't this use repository layout instead?

        String repositoryPath = context.getRepository().getAbsolutePath();
        String artifactPath = file.getAbsolutePath();
        
        // protection from IndexOutOfBounds
        if ( artifactPath.length() <= repositoryPath.length() ) 
        {
            return null;  // not an artifact
        }
        
        if ( !AbstractIndexCreator.isIndexable( file ) )
        {
            return null;  // skipped
        }

        String path = artifactPath.substring( repositoryPath.length() + 1 ).replace( '\\', '/' );
        
        Gav gav = context.getGavCalculator().pathToGav( path );

        if ( gav == null )
        {
            return null; // not an artifact
        }

        String groupId = gav.getGroupId();

        String artifactId = gav.getArtifactId();

        String version = gav.getBaseVersion();

        String classifier = gav.getClassifier();

        ArtifactInfo ai = new ArtifactInfo( context.getRepositoryId(), groupId, artifactId, version, classifier );
        
        // store extension if classifier is not empty
        if ( !StringUtils.isEmpty( ai.classifier ) )
        {
            ai.packaging = gav.getExtension();
        }
        
        ai.fextension = gav.getExtension();
        ai.fname = file.getName();
        
        File pom;
        File artifact;

        if ( file.getName().endsWith( ".pom" ) )
        {
            // there is no "reliable" way to go from pom to artifact.
            // if artifact exists, scan will find it and will "update" existing Lucene document.
            // API consumers should send artifacts to being added to index, not only poms.
            artifact = null;
            pom = file;
        }
        else
        {
            artifact = file;
            pom = pl.locate( file, context.getGavCalculator(), gav );
        }

        File metadata = ml.locate( pom );

        return new ArtifactContext( pom, artifact, metadata, ai, gav );
    }

}
