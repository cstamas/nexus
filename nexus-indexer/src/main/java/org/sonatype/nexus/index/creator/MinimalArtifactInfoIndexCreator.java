/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.creator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptorBuilder;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.index.ArtifactAvailablility;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.ArtifactIndexingContext;
import org.sonatype.nexus.index.locator.JavadocLocator;
import org.sonatype.nexus.index.locator.Locator;
import org.sonatype.nexus.index.locator.Sha1Locator;
import org.sonatype.nexus.index.locator.SignatureLocator;
import org.sonatype.nexus.index.locator.SourcesLocator;

/**
 * A minimal index creator used to provide basic information about Maven artifact.
 * 
 * @plexus.component role-hint="min"
 */
public class MinimalArtifactInfoIndexCreator
    extends AbstractIndexCreator
{
    private ModelReader modelReader = new ModelReader();

    private Locator jl = new JavadocLocator();

    private Locator sl = new SourcesLocator();

    private Locator sigl = new SignatureLocator();

    private Locator sha1l = new Sha1Locator();

    public void populateArtifactInfo( ArtifactIndexingContext aic )
    {
        ArtifactContext ac = aic.getArtifactContext();

        File artifact = ac.getArtifact();

        File pom = ac.getPom();

        ArtifactInfo ai = ac.getArtifactInfo();

        // boolean isSnapshot = VersionUtils.isSnapshot( ai.version );

        if ( pom != null )
        {
            Model model = modelReader.readModel( pom, ai.groupId, ai.artifactId, ai.version );

            if ( model != null )
            {
                ai.name = model.getName();

                ai.description = model.getDescription();

                if ( model.getPackaging() != null && ai.classifier == null )
                {
                    // only when this is not a classified artifact
                    ai.packaging = model.getPackaging();
                }
            }

            ai.lastModified = pom.lastModified();

            ai.fextension = "pom";

//            ai.getArtifacts().add( new Gav( //
//                ai.groupId, //
//                ai.artifactId, //
//                ai.version, //
//                null, // classifier
//                "pom", // extension
//                null, //
//                null, //
//                pom.getName(), // name
//                isSnapshot,
//                false,
//                null,
//                false,
//                null) );
        }

        // TODO handle artifacts without poms
        if ( pom != null )
        {
            if ( ai.classifier != null )
            {
                ai.sourcesExists = ArtifactAvailablility.NOT_AVAILABLE;

                ai.javadocExists = ArtifactAvailablility.NOT_AVAILABLE;
            }
            else
            {
                File sources = sl.locate( pom );
                if ( !sources.exists() )
                {
                    ai.sourcesExists = ArtifactAvailablility.NOT_PRESENT;
                }
                else
                {
                    ai.sourcesExists = ArtifactAvailablility.PRESENT;

//                    ai.getArtifacts().add( new Gav( //
//                        ai.groupId, //
//                        ai.artifactId, //
//                        ai.version, //
//                        "sources", // classifier
//                        "jar", // extension
//                        null, //
//                        null, //
//                        sources.getName(), // name
//                        isSnapshot,
//                        false,
//                        null,
//                        false,
//                        null) );
                }

                File javadoc = jl.locate( pom );
                if ( !javadoc.exists() )
                {
                    ai.javadocExists = ArtifactAvailablility.NOT_PRESENT;
                }
                else
                {
                    ai.javadocExists = ArtifactAvailablility.PRESENT;

//                    ai.getArtifacts().add( new Gav( //
//                        ai.groupId, //
//                        ai.artifactId, //
//                        ai.version, //
//                        "javadoc", // classifier
//                        "jar", // extension
//                        null, //
//                        null, //
//                        javadoc.getName(), // name
//                        isSnapshot,
//                        false,
//                        null,
//                        false,
//                        null) );
                }
            }
        }

        if ( artifact != null )
        {
            // ai.getArtifacts().add( ac.getGav() );

            File signature = sigl.locate( artifact );
            ai.signatureExists = signature.exists() ? ArtifactAvailablility.PRESENT : ArtifactAvailablility.NOT_PRESENT;

            File sha1 = sha1l.locate( artifact );

            if ( sha1.exists() )
            {
                try
                {
                    ai.sha1 = StringUtils.chomp( FileUtils.fileRead( sha1 ) ).trim().split( " " )[0];
                }
                catch ( IOException e )
                {
                    ac.addError( e );
                }
            }

            ai.lastModified = artifact.lastModified();

            ai.size = artifact.length();

            ai.fextension = getExtension( artifact, ac.getGav() );

            if ( ai.packaging == null )
            {
                ai.packaging = ai.fextension;
            }
        }

        checkMavenPlugin( ai, artifact );
    }

    private String getExtension( File artifact, Gav gav )
    {
        if ( gav != null )
        {
            return gav.getExtension();
        }

        // last resort, the extension of the file
        String artifactFileName = artifact.getName().toLowerCase();

        // tar.gz? and other "special" combinations?
        if ( artifactFileName.endsWith( "tar.gz" ) )
        {
            return "tar.gz";
        }
        else if ( artifactFileName.equals( "tar.bz2" ) )
        {
            return "tar.bz2";
        }

        // javadoc: gets the part _AFTER_ last dot!
        return FileUtils.getExtension( artifactFileName );
    }

    private void checkMavenPlugin( ArtifactInfo ai, File artifact )
    {
        if ( !"maven-plugin".equals( ai.packaging ) || artifact == null )
        {
            return;
        }

        ZipFile jf = null;

        InputStream is = null;

        try
        {
            jf = new ZipFile( artifact );

            ZipEntry entry = jf.getEntry( "META-INF/maven/plugin.xml" );

            if ( entry != null )
            {
                is = new BufferedInputStream( jf.getInputStream( entry ) );

                PluginDescriptorBuilder builder = new PluginDescriptorBuilder();

                PluginDescriptor descriptor = builder.build( new InputStreamReader( is ) );

                ai.prefix = descriptor.getGoalPrefix();

                ai.goals = new ArrayList<String>();

                for ( Object o : descriptor.getMojos() )
                {
                    ai.goals.add( ( (MojoDescriptor) o ).getGoal() );
                }
            }
        }
        catch ( Exception e )
        {
        }
        finally
        {
            close( jf );
            IOUtil.close( is );
        }
    }

    public void updateDocument( ArtifactIndexingContext context, Document doc )
    {
        ArtifactInfo ai = context.getArtifactContext().getArtifactInfo();

        String info = new StringBuilder()
            .append( ai.packaging ).append( AbstractIndexCreator.FS ).append( Long.toString( ai.lastModified ) )
            .append( AbstractIndexCreator.FS ).append( Long.toString( ai.size ) ).append( AbstractIndexCreator.FS )
            .append( ai.sourcesExists.toString() ).append( AbstractIndexCreator.FS ).append(
                ai.javadocExists.toString() ).append( AbstractIndexCreator.FS ).append( ai.signatureExists.toString() )
            .append( AbstractIndexCreator.FS ).append( ai.fextension ).toString();

        doc.add( new Field( ArtifactInfo.INFO, info, Field.Store.YES, Field.Index.NO ) );

        doc.add( new Field( ArtifactInfo.GROUP_ID, ai.groupId, Field.Store.NO, Field.Index.UN_TOKENIZED ) );

        doc.add( new Field( ArtifactInfo.ARTIFACT_ID, ai.artifactId, Field.Store.NO, Field.Index.TOKENIZED ) );

        doc.add( new Field( ArtifactInfo.VERSION, ai.version, Field.Store.NO, Field.Index.TOKENIZED ) );

        if ( ai.name != null )
        {
            doc.add( new Field( ArtifactInfo.NAME, ai.name, Field.Store.YES, Field.Index.NO ) );
        }

        if ( ai.description != null )
        {
            doc.add( new Field( ArtifactInfo.DESCRIPTION, ai.description, Field.Store.YES, Field.Index.NO ) );
        }

        if ( ai.packaging != null )
        {
            doc.add( new Field( ArtifactInfo.PACKAGING, ai.packaging, Field.Store.NO, Field.Index.UN_TOKENIZED ) );
        }

        if ( ai.prefix != null )
        {
            doc.add( new Field( ArtifactInfo.PLUGIN_PREFIX, ai.prefix, Field.Store.YES, Field.Index.NO ) );
        }

        if ( ai.goals != null )
        {
            doc.add( new Field( ArtifactInfo.PLUGIN_GOALS, lst2str( ai.goals ), Field.Store.YES, Field.Index.NO ) );
        }

        if ( ai.sha1 != null )
        {
            doc.add( new Field( ArtifactInfo.SHA1, ai.sha1, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
        }
    }

    public boolean updateArtifactInfo( Document doc, ArtifactInfo ai )
    {
        boolean res = false;

        String uinfo = doc.get( ArtifactInfo.UINFO );

        if ( uinfo != null )
        {
            String[] r = FS_PATTERN.split( uinfo );

            ai.groupId = r[0];

            ai.artifactId = r[1];

            ai.version = r[2];

            if ( r.length > 3 )
            {
                ai.classifier = renvl( r[3] );
            }

            res = true;
        }

        String info = doc.get( ArtifactInfo.INFO );

        if ( info != null )
        {
            String[] r = FS_PATTERN.split( info );

            ai.packaging = r[0];

            ai.lastModified = Long.parseLong( r[1] );

            ai.size = Long.parseLong( r[2] );

            ai.sourcesExists = ArtifactAvailablility.fromString( r[3] );

            ai.javadocExists = ArtifactAvailablility.fromString( r[4] );

            ai.signatureExists = ArtifactAvailablility.fromString( r[5] );

            if ( r.length > 6 )
            {
                ai.fextension = r[6];
            }
            else
            {
                if ( ai.classifier != null || "pom".equals( ai.packaging ) || "war".equals( ai.packaging )
                    || "ear".equals( ai.packaging ) )
                {
                    ai.fextension = ai.packaging;
                }
                else
                {
                    ai.fextension = "jar"; // best guess
                }
            }

            if ( "maven-plugin".equals( ai.packaging ) )
            {
                ai.prefix = doc.get( ArtifactInfo.PLUGIN_PREFIX );

                String goals = doc.get( ArtifactInfo.PLUGIN_GOALS );

                if ( goals != null )
                {
                    ai.goals = str2lst( goals );
                }
            }

            res = true;
        }

        String name = doc.get( ArtifactInfo.NAME );

        if ( name != null )
        {
            ai.name = name;

            res = true;
        }

        String description = doc.get( ArtifactInfo.DESCRIPTION );

        if ( description != null )
        {
            ai.description = description;

            res = true;
        }

        // sometimes there's a pom without packaging(default to jar), but no artifact, then the value will be a "null"
        // String
        if ( "null".equals( ai.packaging ) )
        {
            ai.packaging = null;
        }

        String sha1 = doc.get( ArtifactInfo.SHA1 );

        if ( sha1 != null )
        {
            ai.sha1 = sha1;
        }

        return res;

        // artifactInfo.fname = ???

    }

    private void close( ZipFile zf )
    {
        if ( zf != null )
        {
            try
            {
                zf.close();
            }
            catch ( IOException ex )
            {
            }
        }
    }

    /**
     * Caching lightweight model reader
     */
    public static class ModelReader
    {
        private final HashMap<File, Model> models = new HashMap<File, Model>();

        public Model getModel( File pom, String groupId, String artifactId, String version )
        {
            Model model = models.get( pom );
            if ( model == null )
            {
                model = readModel( pom, groupId, artifactId, version );
                models.put( pom, model );
            }
            return model;
        }

        public Model readModel( File pom, String groupId, String artifactId, String version )
        {
            Xpp3Dom dom = readPom( pom );

            if ( dom == null )
            {
                return null;
            }

            String packaging = null;

            if ( dom.getChild( "packaging" ) != null )
            {
                packaging = dom.getChild( "packaging" ).getValue();
            }

            // Xpp3Dom parent = dom.getChild( "parent" );
            //
            // if ( parent != null )
            // {
            // String parentGroupId = parent.getChild( "groupId" ).getValue();
            //
            // String parentArtifactId = parent.getChild( "artifactId" ).getValue();
            //
            // String parentVersion = parent.getChild( "version" ).getValue();
            //
            // String parentPomPath = getPath( parentGroupId, parentArtifactId, parentVersion, artifactId + "-"
            // + version + ".pom" );
            //
            // String repository = getRepository( groupId, artifactId, version, pom );
            //
            // // if ( repository != null )
            // // {
            // // Model parentModel = getModel( new File( repository, parentPomPath ), parentGroupId, parentArtifactId,
            // // parentVersion );
            // //
            // // if ( parentModel !=null )
            // // {
            // // //
            // // }
            // // }
            // }

            Model model = new Model();

            model.setPackaging( packaging );

            if ( dom.getChild( "name" ) != null )
            {
                model.setName( dom.getChild( "name" ).getValue() );
            }

            if ( dom.getChild( "description" ) != null )
            {
                model.setDescription( dom.getChild( "description" ).getValue() );
            }

            return model;
        }

        // private String getRepository( String groupId, String artifactId, String version, File pom )
        // {
        // String pomPath = getPath( groupId, artifactId, version, pom.getName() );
        //
        // String fullPomPath = pom.getAbsolutePath();
        //
        // int n = fullPomPath.replace( '\\', '/' ).indexOf( pomPath.replace( '\\', '/' ) );
        //
        // if ( n == -1 )
        // {
        // return null;
        // }
        //
        // return fullPomPath.substring( 0, n );
        // }
        //
        // private String getPath( String groupId, String artifactId, String version, String fname )
        // {
        // return new StringBuilder()
        // .append( groupId.replace( '.', File.separatorChar ) ).append( File.separatorChar ).append( artifactId )
        // .append( File.separatorChar ).append( version ).append( File.separatorChar ).append( fname ).toString();
        // }

        private Xpp3Dom readPom( File pom )
        {
            Reader r = null;
            try
            {
                r = new FileReader( pom );

                return Xpp3DomBuilder.build( r );
            }
            catch ( Exception e )
            {
                // e.printStackTrace();
            }
            finally
            {
                if ( r != null )
                {
                    try
                    {
                        r.close();
                    }
                    catch ( IOException ex )
                    {
                    }
                }
            }
            return null;
        }

    }

    @Override
    public String toString()
    {
        return "min";
    }

}
