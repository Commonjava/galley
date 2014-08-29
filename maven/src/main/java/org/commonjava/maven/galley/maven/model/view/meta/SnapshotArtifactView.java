package org.commonjava.maven.galley.maven.model.view.meta;

import java.text.ParseException;
import java.util.Date;

import org.commonjava.maven.atlas.ident.util.SnapshotUtils;
import org.w3c.dom.Element;

public class SnapshotArtifactView
    extends MavenMetadataElementView
{

    public SnapshotArtifactView( final MavenMetadataView xmlView, final Element element )
    {
        super( xmlView, element );
    }

    public String getExtension()
    {
        return getValue( "extension" );
    }

    public Date getUpdated()
        throws ParseException
    {
        final String val = getValue( "updated" );
        return val == null ? null : SnapshotUtils.parseUpdateTimestamp( val );
    }

    public String getClassifier()
    {
        return getValue( "classifier" );
    }

    public String getVersion()
    {
        return getValue( "value" );
    }

}
