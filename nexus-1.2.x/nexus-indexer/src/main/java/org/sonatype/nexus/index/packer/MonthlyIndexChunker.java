package org.sonatype.nexus.index.packer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Monthly chunker. This Chunker will cut the index into months, based on LAST_MODIFIED, the timestamp when the file get
 * to index (not the filetimestamp!).
 * 
 * @author Tamas Cservenak
 * @plexus.component role-hint="month"
 */
public class MonthlyIndexChunker
    extends AbstractIndexChunker
{
    private static final String ID = "month";

    private static final String INDEX_TIME_DAY_FORMAT = "yyyyMM";

    private final SimpleDateFormat df;

    public MonthlyIndexChunker()
    {
        this.df = new SimpleDateFormat( INDEX_TIME_DAY_FORMAT );
        this.df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    }

    public String getId()
    {
        return ID;
    }

    @Override
    protected DateFormat getDateFormat()
    {
        return df;
    }
}
