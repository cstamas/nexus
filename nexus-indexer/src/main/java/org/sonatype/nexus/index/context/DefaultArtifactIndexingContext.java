/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.context;

import org.sonatype.nexus.index.ArtifactContext;

/** @author Jason van Zyl */
public class DefaultArtifactIndexingContext
    implements ArtifactIndexingContext
{
    private ArtifactContext artifactContext;

    public DefaultArtifactIndexingContext( ArtifactContext artifactContext )
    {
        this.artifactContext = artifactContext;
    }

    public ArtifactContext getArtifactContext()
    {
        return artifactContext;
    }
}
