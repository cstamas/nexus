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
package org.sonatype.nexus.integrationtests.nexus261;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.testng.Assert;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.testng.annotations.Test;

/**
 * Tests to make sure an artifact deployed in multiple repositories will respect the group order. 
 */
public class Nexus261NexusGroupDownloadTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void downloadArtifact()
        throws IOException
    {
        Gav gav =
            new Gav( this.getTestId(), "release-jar", "1", null, "jar", 0, new Date().getTime(),
                     "Release Jar", false, false, null, false, null );

        File artifact = downloadArtifactFromGroup( "nexus-test", gav, "./target/downloaded-jars" );

        Assert.assertTrue( artifact.exists() );

        File originalFile =
            this.getTestResourceAsFile( "projects/" + gav.getArtifactId() + "/" + gav.getArtifactId() + "."
                + gav.getExtension() );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, artifact ) );

    }
}
