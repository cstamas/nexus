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
package org.sonatype.nexus.index.locator;

import java.io.File;

/** @author Jason van Zyl */
public class SignatureLocator
    implements Locator
{
    public File locate( File source )
    {
        // return new File( source.getParentFile(), source.getName() + ".asc" );
        return new File( source.getAbsolutePath() + ".asc" );
    }
}
