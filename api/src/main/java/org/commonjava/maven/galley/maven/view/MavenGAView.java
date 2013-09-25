package org.commonjava.maven.galley.maven.view;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.w3c.dom.Element;

public class MavenGAView
    extends MavenElementView
    implements ProjectRefView
{

    private String groupId;

    private String artifactId;

    public MavenGAView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        super( pomView, element, managementXpathFragment );
    }

    public MavenGAView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element, null );
    }

    @Override
    public synchronized String getGroupId()
    {
        if ( groupId == null )
        {
            groupId = getValue( G );
        }

        return groupId;
    }

    protected void setGroupId( final String groupId )
    {
        this.groupId = groupId;
    }

    @Override
    public synchronized String getArtifactId()
    {
        if ( artifactId == null )
        {
            artifactId = getValue( A );
        }

        return artifactId;
    }

    @Override
    public ProjectRef asProjectRef()
    {
        return new ProjectRef( getGroupId(), getArtifactId() );
    }

    @Override
    public String toString()
    {
        return String.format( "%s [%s:%s]", getClass().getSimpleName(), getGroupId(), getArtifactId() );
    }

    public boolean isValid()
    {
        return !containsExpression( getGroupId() ) && !containsExpression( getArtifactId() );
    }

}
