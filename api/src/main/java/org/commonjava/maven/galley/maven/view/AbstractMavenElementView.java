package org.commonjava.maven.galley.maven.view;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractMavenElementView
{

    protected static final String G = "groupId";

    protected static final String A = "artifactId";

    protected static final String V = "version";

    protected static final String TEXTEQ = "/text()=\"";

    protected final Element element;

    protected final MavenPomView pomView;

    private final String managementXpathFragment;

    private String managementXpath;

    protected AbstractMavenElementView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        this.pomView = pomView;
        this.element = element;
        this.managementXpathFragment = managementXpathFragment;
    }

    protected abstract String getManagedViewQualifierFragment()
        throws GalleyMavenException;

    public Element getElement()
    {
        return element;
    }

    public MavenPomView getPomView()
    {
        return pomView;
    }

    public String getProfileId()
    {
        return pomView.getProfileIdFor( element );
    }

    protected String getValueWithManagement( final String named )
        throws GalleyMavenException
    {
        String value = getValue( named );
        if ( value == null )
        {
            final String xpath = managementXpathFor( named + "/text()" );
            if ( xpath != null )
            {
                value = pomView.resolveXPathExpression( xpath, false );
            }
        }

        return value;
    }

    protected Element getElementWithManagement( final String named )
        throws GalleyMavenException
    {
        Element value = (Element) getNode( named );
        if ( value == null )
        {
            final String xpath = managementXpathFor( named );
            if ( xpath != null )
            {
                value = (Element) pomView.resolveXPathToNode( xpath, false );
            }
        }

        return value;
    }

    private String managementXpathFor( final String named )
        throws GalleyMavenException
    {
        initManagementXpath();
        if ( managementXpath == null )
        {
            return null;
        }

        return String.format( managementXpath, named, named );
    }

    private void initManagementXpath()
        throws GalleyMavenException
    {
        if ( managementXpathFragment == null )
        {
            return;
        }

        final String qualifier = getManagedViewQualifierFragment();
        if ( qualifier == null )
        {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append( "ancestor::project/" )
          .append( managementXpathFragment )
          .append( '[' )
          .append( qualifier )
          .append( "]/%s|ancestor::profile/" )
          .append( managementXpathFragment )
          .append( '[' )
          .append( qualifier )
          .append( "]/%s" );

        managementXpath = sb.toString();
    }

    protected String getValue( final String path )
        throws GalleyMavenException
    {
        final String[] names = path.split( "/" );
        Element e = element;
        for ( final String named : names )
        {
            if ( e == null )
            {
                break;
            }

            final NodeList matches = e.getElementsByTagName( named );
            if ( matches == null || matches.getLength() < 1 )
            {
                return null;
            }

            e = (Element) matches.item( 0 );
        }

        return e == null ? null : pomView.resolveExpressions( e.getTextContent()
                                                               .trim() );
    }

    protected Node getNode( final String path )
    {
        final String[] names = path.split( "/" );
        Element e = element;
        for ( final String named : names )
        {
            if ( e == null )
            {
                break;
            }

            final NodeList matches = e.getElementsByTagName( named );
            if ( matches == null || matches.getLength() < 1 )
            {
                return null;
            }

            e = (Element) matches.item( 0 );
        }

        return e;
    }

}
