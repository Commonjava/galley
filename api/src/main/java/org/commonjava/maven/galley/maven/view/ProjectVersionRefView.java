package org.commonjava.maven.galley.maven.view;

import org.w3c.dom.Element;

public class ProjectVersionRefView
    extends AbstractMavenGAVView
{

    protected ProjectVersionRefView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element, null );
    }

    @Override
    protected String getManagedViewQualifierFragment()
    {
        return null;
    }

}
