package org.sonatype.nexus.proxy.registry.validation;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

public abstract class AbstractFileTypeValidator
    implements FileTypeValidator
{
    @Requirement
    private Logger logger;

    private MimeUtil2 magicMimeUtil = new MimeUtil2();

    public AbstractFileTypeValidator()
    {
        magicMimeUtil.registerMimeDetector( MagicMimeMimeDetector.class.getName() );
    }

    protected abstract Map<String, String> getFileTypeToMimeMap();

    public Set<String> getSupportedFileTypesForValidation()
    {
        return getFileTypeToMimeMap().keySet();
    }

    public boolean isExpectedFileType( InputStream inputStream, String actualFileName )
    {
        Set<String> expectedMimeTypes = new HashSet<String>();
        for ( Entry<String, String> entry : this.getFileTypeToMimeMap().entrySet() )
        {
            if ( actualFileName.endsWith( entry.getKey() ) )
            {
                expectedMimeTypes.add( entry.getValue() );
            }
        }

        if ( this.logger.isDebugEnabled() )
        {
            this.logger.debug( "Checking if file: " + actualFileName + " is of one of the types: " + expectedMimeTypes );
        }
        return isExpectedFileType( inputStream, expectedMimeTypes );
    }

    @SuppressWarnings( "unchecked" )
    protected boolean isExpectedFileType( InputStream inputStream, Set<String> expectedMimeTypes )
    {
        Collection<MimeType> magicMimeTypes = magicMimeUtil.getMimeTypes( new BufferedInputStream( inputStream ) );

        if ( this.logger.isDebugEnabled() )
        {
            this.logger.debug( "Expected mime types: " + expectedMimeTypes + ", Actual mime types: " + magicMimeTypes );
        }

        for ( MimeType magicMimeType : magicMimeTypes )
        {
            if ( expectedMimeTypes.contains( magicMimeType.toString() ) )
            {
                return true;
            }
        }
        return false;
    }
}
