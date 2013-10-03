package org.commonjava.maven.galley.maven.model.view;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.version.InvalidVersionSpecificationException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;

public class MavenGAVView
    extends MavenGAView
    implements ProjectVersionRefView
{

    private String version;

    public MavenGAVView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        super( pomView, element, managementXpathFragment );
    }

    public MavenGAVView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element );
    }

    @Override
    public synchronized String getVersion()
        throws GalleyMavenException
    {
        if ( version == null )
        {
            //            final Logger logger = new Logger( getClass() );
            //            logger.info( "Resolving version for: %s[%s:%s]\nIn: %s", getClass().getSimpleName(), getGroupId(), getArtifactId(), pomView.getRef() );
            version = getValueWithManagement( V );
        }

        return version;
    }

    @Override
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

    @Override
    public boolean isValid()
    {
        try
        {
            return super.isValid() && !containsExpression( getVersion() ) && asProjectVersionRef().getVersionSpec() != null;
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            return false;
        }
    }

}
