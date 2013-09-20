package org.commonjava.maven.galley.maven.view;

import org.w3c.dom.Element;

public class ExtensionView
    extends ProjectVersionRefView
{

    protected ExtensionView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element );
    }

}
