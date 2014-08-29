package org.commonjava.maven.galley.maven.model.view.meta;

import org.commonjava.maven.galley.maven.model.view.AbstractMavenElementView;
import org.w3c.dom.Element;

public class MavenMetadataElementView
    extends AbstractMavenElementView<MavenMetadataView>
{

    public MavenMetadataElementView( final MavenMetadataView xmlView, final Element element )
    {
        super( xmlView, element );
    }

}
