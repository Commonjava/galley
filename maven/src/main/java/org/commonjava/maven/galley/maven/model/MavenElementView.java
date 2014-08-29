package org.commonjava.maven.galley.maven.model;

import org.commonjava.maven.galley.maven.model.view.MavenPomElementView;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.w3c.dom.Element;

@Deprecated
public class MavenElementView
    extends MavenPomElementView
{

    public MavenElementView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        super( pomView, element, managementXpathFragment );
    }

    public MavenElementView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element );
    }

}
