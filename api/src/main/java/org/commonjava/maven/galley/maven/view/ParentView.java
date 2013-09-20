package org.commonjava.maven.galley.maven.view;

import org.w3c.dom.Element;

public class ParentView
    extends ProjectVersionRefView
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
