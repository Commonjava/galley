package org.commonjava.maven.galley.maven.view;

import java.util.HashSet;
import java.util.Set;

import org.commonjava.maven.atlas.ident.DependencyScope;
import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.VersionlessArtifactRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DependencyView
    extends AbstractMavenGAVView
{

    private static final String C = "classifier";

    private static final String T = "type";

    private static final String S = "scope";

    private static final String OPTIONAL = "optional";

    private static final String EXCLUSIONS = "exclusions";

    private static final String EXCLUSION = "exclusions";

    private String classifier;

    private String type;

    private DependencyScope scope;

    private Boolean optional;

    private Set<ProjectRefView> exclusions;

    public DependencyView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element, "dependencyManagement/dependencies/dependency" );
    }

    public boolean isManaged()
    {
        return pomView.resolveXPathToNodeFrom( element, "ancestor::dependencyManagement" ) != null;
    }

    public synchronized String getClassifier()
        throws GalleyMavenException
    {
        if ( classifier == null )
        {
            classifier = getValue( C );
        }

        return classifier;
    }

    public synchronized String getType()
        throws GalleyMavenException
    {
        if ( type == null )
        {
            type = getValue( T );
        }

        return type;
    }

    public synchronized DependencyScope getScope()
        throws GalleyMavenException
    {
        if ( scope == null )
        {
            final String s = getValueWithManagement( S );
            scope = DependencyScope.getScope( s );
        }

        return scope;
    }

    public synchronized boolean isOptional()
    {
        if ( optional == null )
        {
            final String val = getValue( OPTIONAL );
            optional = val == null ? false : Boolean.parseBoolean( val );
        }

        return optional;
    }

    public synchronized Set<ProjectRefView> getExclusions()
        throws GalleyMavenException
    {
        if ( exclusions == null )
        {
            final Element exNode = getElementWithManagement( EXCLUSIONS );
            if ( exNode != null )
            {
                final Set<ProjectRefView> exclusions = new HashSet<>();
                final NodeList exNodes = exNode.getElementsByTagName( EXCLUSION );
                for ( int i = 0; i < exNodes.getLength(); i++ )
                {
                    final Element exclusion = (Element) exNodes.item( i );
                    exclusions.add( new ProjectRefView( pomView, exclusion ) );
                }

                this.exclusions = exclusions;
            }
        }

        return exclusions;
    }

    @Override
    protected String getManagedViewQualifierFragment()
        throws GalleyMavenException
    {
        final StringBuilder sb = new StringBuilder();

        sb.append( G )
          .append( TEXTEQ )
          .append( getGroupId() )
          .append( "\" and " )
          .append( A )
          .append( TEXTEQ )
          .append( getArtifactId() )
          .append( "\"" );

        final String cls = getClassifier();
        if ( cls != null )
        {
            sb.append( " and " )
              .append( C )
              .append( TEXTEQ )
              .append( cls )
              .append( "\"" );
        }

        final String type = getType();
        if ( type != null )
        {
            sb.append( " and " )
              .append( T )
              .append( TEXTEQ )
              .append( type )
              .append( "\"" );
        }

        return sb.toString();
    }

    public ArtifactRef asArtifactRef()
    {
        return new ArtifactRef( asProjectVersionRef(), getType(), getClassifier(), isOptional() );
    }

    public VersionlessArtifactRef asVersionlessArtifactRef()
    {
        return new VersionlessArtifactRef( asProjectRef(), getType(), getClassifier(), isOptional() );
    }

}
