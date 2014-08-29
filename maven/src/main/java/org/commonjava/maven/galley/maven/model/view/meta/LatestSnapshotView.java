package org.commonjava.maven.galley.maven.model.view.meta;

import java.text.ParseException;
import java.util.Date;

import org.commonjava.maven.atlas.ident.util.SnapshotUtils;
import org.w3c.dom.Element;

public class LatestSnapshotView
    extends MavenMetadataElementView
{

    public LatestSnapshotView( final MavenMetadataView xmlView, final Element element )
    {
        super( xmlView, element );
    }

    public boolean isLocalCopy()
    {
        final String val = getValue( "localCopy" );
        return val == null ? false : Boolean.parseBoolean( val );
    }

    public Date getTimestamp()
        throws ParseException
    {
        final String val = getValue( "timestamp" );
        return val == null ? null : SnapshotUtils.parseSnapshotTimestamp( val );
    }

    public Integer getBuildNumber()
    {
        final String val = getValue( "buildNumber" );
        return val == null ? null : Integer.parseInt( val );
    }

}
