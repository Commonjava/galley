package org.commonjava.maven.galley.maven.model.view;

import org.w3c.dom.Element;

public class RepositoryView
    extends AbstractMavenElementView<MavenPomView>
{

    private String url;

    private String name;

    public RepositoryView( final MavenPomView xmlView, final Element element )
    {
        super( xmlView, element );
    }

    public synchronized String getUrl()
    {
        if ( url == null )
        {
            url = getValue( "url/text()" );
        }

        return url;
    }

    public synchronized String getName()
    {
        if ( name == null )
        {
            name = getValue( "name/text()" );
        }

        return name;
    }

}
