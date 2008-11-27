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
package org.sonatype.nexus.index.scan;

import java.util.ArrayList;
import java.util.List;

/** @author Jason van Zyl */
public class DefaultScanningResult
    implements ScanningResult
{
    private int totalFiles = 0;

    private List<Exception> exceptions = new ArrayList<Exception>();

    public void setTotalFiles( int totalFiles )
    {
        this.totalFiles = totalFiles;
    }

    public void incrementCount()
    {
        totalFiles++;
    }

    public int getTotalFiles()
    {
        return totalFiles;
    }

    public void addException( Exception e )
    {
        exceptions.add( e );
    }

    public boolean hasExceptions()
    {
        return exceptions.size() != 0;
    }

    public List<Exception> getExceptions()
    {
        return exceptions;
    }

}
