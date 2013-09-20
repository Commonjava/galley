package org.commonjava.maven.galley.maven.view;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;

public abstract class AbstractMavenGAVView
    extends AbstractMavenGAView
{

    private String version;

    protected AbstractMavenGAVView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        super( pomView, element, managementXpathFragment );
    }

    public synchronized String getVersion()
        throws GalleyMavenException
    {
        if ( version == null )
        {
            version = getValueWithManagement( V );
        }

        return version;
    }

    public ProjectVersionRef asProjectVersionRef()
        throws GalleyMavenException
    {
        return new ProjectVersionRef( getGroupId(), getArtifactId(), getVersion() );
    }

    protected void setVersion( final String version )
    {
        this.version = version;
    }

    @Override
    public String toString()
    {
        return String.format( "%s [%s:%s:%s]", getClass().getSimpleName(), getGroupId(), getArtifactId(), version == null ? "unresolved" : version );
    }

}
