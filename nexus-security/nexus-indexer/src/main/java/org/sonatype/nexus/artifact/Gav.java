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
package org.sonatype.nexus.artifact;

public class Gav
{
    public enum HashType
    {
        sha1, md5
    };

    private String groupId;

    private String artifactId;

    private String version;

    private String baseVersion;

    private String classifier;

    private String extension;

    private Integer snapshotBuildNumber;

    private Long snapshotTimeStamp;

    private String name;

    private boolean snapshot;

    private boolean hash;

    private HashType hashType;

    public Gav( String groupId, String artifactId, String version, String baseVersion, String classifier,
        String extension, Integer snapshotBuildNumber, Long snapshotTimeStamp, String name, boolean snapshot,
        boolean hash, HashType hashType )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;

        if ( baseVersion == null )
        {
            this.version = version;
            this.baseVersion = null;
        }
        else
        {
            this.version = version;
            this.baseVersion = baseVersion;
        }

        this.classifier = classifier;
        this.extension = extension;
        this.snapshotBuildNumber = snapshotBuildNumber;
        this.snapshotTimeStamp = snapshotTimeStamp;
        this.name = name;
        this.snapshot = snapshot;
        this.hash = hash;
        this.hashType = hashType;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getBaseVersion()
    {
        if ( baseVersion == null )
        {
            return getVersion();
        }
        else
        {
            return baseVersion;
        }
    }

    public String getClassifier()
    {
        return classifier;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getName()
    {
        return name;
    }

    public boolean isSnapshot()
    {
        return snapshot;
    }

    public Integer getSnapshotBuildNumber()
    {
        return snapshotBuildNumber;
    }

    public boolean isHash()
    {
        return hash;
    }

    public Long getSnapshotTimeStamp()
    {
        return snapshotTimeStamp;
    }

    public HashType getHashType()
    {
        return hashType;
    }

}
