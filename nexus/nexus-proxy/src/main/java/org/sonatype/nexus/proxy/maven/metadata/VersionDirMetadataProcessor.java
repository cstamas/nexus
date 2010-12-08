package org.sonatype.nexus.proxy.maven.metadata;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataException;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.SetSnapshotOperation;
import org.sonatype.nexus.proxy.maven.metadata.operations.StringOperand;

/**
 * Process maven metadata in snapshot version directory
 * 
 * @author juven
 */
public class VersionDirMetadataProcessor
    extends AbstractMetadataProcessor
{
    public VersionDirMetadataProcessor( AbstractMetadataHelper metadataHelper )
    {
        super( metadataHelper );
    }

    @Override
    public boolean shouldProcessMetadata( String path )
    {

        Collection<String> names = metadataHelper.gavData.get( path );

        if ( names != null && !names.isEmpty() )
        {
            return true;
        }

        return false;
    }

    @Override
    public void processMetadata( String path )
        throws Exception
    {
        Metadata md = createMetadata( path );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        MetadataBuilder.write( md, outputStream );

        String mdString = outputStream.toString();

        outputStream.close();

        metadataHelper.store( mdString, path + AbstractMetadataHelper.METADATA_SUFFIX );
    }

    private Metadata createMetadata( String path )
        throws Exception
    {
        Metadata md = new Metadata();

        md.setGroupId( calculateGroupId( path ) );

        md.setArtifactId( calculateArtifactId( path ) );

        md.setVersion( calculateVersion( path ) );

        versioning( md, metadataHelper.gavData.get( path ) );

        return md;
    }

    private String calculateGroupId( String path )
    {
        String gaPath = path.substring( 0, path.lastIndexOf( '/' ) );

        return gaPath.substring( 1, gaPath.lastIndexOf( '/' ) ).replace( '/', '.' );
    }

    private String calculateArtifactId( String path )
    {
        String gaPath = path.substring( 0, path.lastIndexOf( '/' ) );

        return gaPath.substring( gaPath.lastIndexOf( '/' ) + 1 );
    }

    private String calculateVersion( String path )
    {
        return path.substring( path.lastIndexOf( '/' ) + 1 );
    }

    void versioning( Metadata metadata, Collection<String> artifactNames )
        throws MetadataException
    {
        List<MetadataOperation> ops = new ArrayList<MetadataOperation>();

        for ( String artifactName : artifactNames )
        {
            ops.add( new SetSnapshotOperation( new StringOperand( artifactName ) ) );
        }

        MetadataBuilder.changeMetadata( metadata, ops );
    }

    @Override
    public void postProcessMetadata( String path )
    {
        metadataHelper.gavData.remove( path );
    }

    @Override
    protected boolean isMetadataCorrect( Metadata oldMd, String path )
        throws Exception
    {
        if ( oldMd.getArtifactId() == null || oldMd.getGroupId() == null || oldMd.getVersion() == null
            || oldMd.getVersioning() == null || oldMd.getVersioning().getSnapshot() == null
            || oldMd.getVersioning().getSnapshot().getTimestamp() == null )
        {
            return false;
        }

        Metadata md = createMetadata( path );

        if ( StringUtils.equals( oldMd.getArtifactId(), md.getArtifactId() )
            && StringUtils.equals( oldMd.getGroupId(), md.getGroupId() )
            && StringUtils.equals( oldMd.getVersion(), md.getVersion() )
            && md.getVersioning() != null
            && md.getVersioning().getSnapshot() != null
            && StringUtils.equals( oldMd.getVersioning().getSnapshot().getTimestamp(), md.getVersioning().getSnapshot()
                .getTimestamp() )
            && oldMd.getVersioning().getSnapshot().getBuildNumber() == md.getVersioning().getSnapshot()
                .getBuildNumber() )
        {
            return true;
        }

        return false;
    }

}
