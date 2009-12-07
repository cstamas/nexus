/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus;

import java.io.InputStream;

public class NexusStreamResponse
{
    private String name;
    
    private InputStream inputStream;

    private long size;

    private String mimeType;

    private long fromByte;

    private long bytesCount;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public void setInputStream( InputStream inputStream )
    {
        this.inputStream = inputStream;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize( long contentLength )
    {
        this.size = contentLength;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType( String contentType )
    {
        this.mimeType = contentType;
    }

    public long getFromByte()
    {
        return fromByte;
    }

    public void setFromByte( long fromByte )
    {
        this.fromByte = fromByte;
    }

    public long getBytesCount()
    {
        return bytesCount;
    }

    public void setBytesCount( long bytesCount )
    {
        this.bytesCount = bytesCount;
    }
}
