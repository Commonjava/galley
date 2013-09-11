package org.commonjava.maven.galley.maven.view;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.w3c.dom.Element;

public abstract class AbstractMavenGAView
    extends AbstractMavenElementView
{

    private String groupId;

    private String artifactId;

    protected AbstractMavenGAView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        super( pomView, element, managementXpathFragment );
    }

    public synchronized String getGroupId()
    {
        if ( groupId == null )
        {
            groupId = getValue( G );
        }

        return groupId;
    }

    public synchronized String getArtifactId()
    {
        if ( artifactId == null )
        {
            artifactId = getValue( A );
        }

        return artifactId;
    }

    public ProjectRef asProjectRef()
    {
        return new ProjectRef( getGroupId(), getArtifactId() );
    }

    @Override
    public String toString()
    {
        return String.format( "%s [%s:%s]", getClass().getSimpleName(), getGroupId(), getArtifactId() );
    }

}
