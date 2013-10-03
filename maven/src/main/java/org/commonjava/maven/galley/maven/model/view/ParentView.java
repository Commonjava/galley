package org.commonjava.maven.galley.maven.model.view;

import org.w3c.dom.Element;

public class ParentView
    extends MavenGAVView
{

    public ParentView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element );
    }

    public String getRelativePath()
    {
        String val = getValue( "relativePath" );
        if ( val == null )
        {
            val = "../pom.xml";
        }

        return val;
    }

}
