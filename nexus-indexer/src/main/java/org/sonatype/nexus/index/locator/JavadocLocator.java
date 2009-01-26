/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.locator;

import java.io.File;

/** @author Jason van Zyl */
public class JavadocLocator
    implements Locator
{
    /**
     * Locates the sources bundle relative to POM.
     */
    public File locate( File source )
    {
        String path = source.getAbsolutePath();
        return new File( path.substring( 0, path.length()-4 ).concat( "-javadoc.jar" ) );
    }
}
