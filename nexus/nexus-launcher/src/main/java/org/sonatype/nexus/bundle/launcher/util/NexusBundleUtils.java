/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.bundle.launcher.util;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Untar;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.sonatype.nexus.bundle.launcher.internal.AntHelper;


public class NexusBundleUtils {

    private final AntHelper ant;

    @Inject
    NexusBundleUtils(final AntHelper ant) {
        Preconditions.checkNotNull(ant);
        this.ant = ant;
    }

    /**
     * Extract the specified bundle file to a directory, excluding any provided patterns.
     * @param bundleFile the bundle file to extractUsingPlexus
     * @param extractionDir the directory to extractUsingPlexus to
     * @param nexusBundleExcludes the exclusion patterns to be applied during extraction
     */
    public void extractNexusBundle(final File nexusBundle, final File toDir, final List<String> excludes) throws IOException{
        Preconditions.checkNotNull(nexusBundle);
        Preconditions.checkNotNull(toDir);
        Preconditions.checkNotNull(excludes);
        if(!(nexusBundle.getName().endsWith(".zip") || nexusBundle.getName().endsWith(".tar.gz"))){
            throw new IllegalArgumentException("Nexus bundle must be a zip or tar.gz file: " + nexusBundle.getAbsolutePath());
        }
        extractArchive(nexusBundle, toDir, excludes);
    }

    /**
     * Extracts a nexus plugin to the supplied directory.
     * @param nexusPlugin
     * @param toDir the nexus plugin repository
     * @throws IllegalArgumentException if the nexus plugin file does not look like a nexus plugin
     */
    public void extractNexusPlugin(final File nexusPlugin, final File toDir) throws IOException{
        Preconditions.checkNotNull(nexusPlugin);
        Preconditions.checkNotNull(toDir);
        if(!(nexusPlugin.getName().endsWith("bundle.zip"))){
            throw new IllegalArgumentException("Nexus plugin does not loook like a supported format - expected file name to end with 'bundle.zip' - " + nexusPlugin.getAbsolutePath());
        }
        extractArchive(nexusPlugin, toDir, null);
    }


    /**
     * Extracts a sourceFile to a directory, excluding the specified patterns from the extraction
     * @param sourceFile the archive to extract
     * @param toDir where to extract to
     * @param excludes optional exclude patterns
     * @throws IllegalArgumentException if archive sourceFile is not a supported extraction type
     * @throws NullPointException if sourceFile or toDir is null
     * @throws IOException if a problem during extraction
     */
    public void extractArchive(final File sourceFile, final File toDir, final List<String> excludes) throws IOException {
        Preconditions.checkNotNull(sourceFile);
        Preconditions.checkNotNull(toDir);

        final String fileName = sourceFile.getName();
        ant.mkdir(toDir);

        if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
            try {

                final Expand unzip = ant.createTask(Expand.class);
                unzip.setDest(toDir);
                unzip.setSrc(sourceFile);

                // TODO excludes

                unzip.execute();

            } catch (BuildException e) {
                throw new IOException("Unable to unarchive " + sourceFile + " to " + toDir, e);
            }

        } else if (fileName.endsWith(".tar.gz")) {
            try {
                final Untar untar = ant.createTask(Untar.class);
                untar.setDest(toDir);
                untar.setSrc(sourceFile);
                untar.setCompression(((Untar.UntarCompressionMethod)EnumeratedAttribute.getInstance(Untar.UntarCompressionMethod.class, "gzip")));

                // TODO excludes

                untar.execute();

            } catch (BuildException e) {
                throw new IOException("Unable to unarchive " + sourceFile + " to " + toDir, e);
            }

        } else {
            throw new IllegalArgumentException("Archive type could not be determined from name: " + fileName);
        }

    }

}