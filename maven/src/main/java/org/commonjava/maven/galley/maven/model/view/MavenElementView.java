package org.commonjava.maven.galley.maven.model.view;

import java.util.ArrayList;
import java.util.List;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MavenElementView
{

    //    private final Logger logger = new Logger( getClass() );

    protected final Element element;

    protected final MavenPomView pomView;

    private final String managementXpathFragment;

    private String[] managementXpaths;

    private Element managementElement;

    public MavenElementView( final MavenPomView pomView, final Element element, final String managementXpathFragment )
    {
        this.pomView = pomView;
        this.element = element;
        this.managementXpathFragment = managementXpathFragment;
    }

    public MavenElementView( final MavenPomView pomView, final Element element )
    {
        this.pomView = pomView;
        this.element = element;
        this.managementXpathFragment = null;
    }

    /**
     * Override this to provide the xpath fragment used to find management values for this view.
     */
    protected String getManagedViewQualifierFragment()
    {
        return null;
    }

    protected boolean containsExpression( final String value )
    {
        return pomView.containsExpression( value );
    }

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
        final String value = getValueFrom( named, element );
        //        logger.info( "Value of path: '%s' local to: %s is: '%s'\nIn: %s", named, element, value, pomView.getRef() );
        if ( value == null )
        {
            final Element mgmt = getManagementElement();
            if ( mgmt != null )
            {
                return getValueFrom( named, mgmt );
            }
        }

        return value;
    }

    private synchronized Element getManagementElement()
        throws GalleyMavenException
    {
        if ( managementElement == null )
        {
            initManagementXpaths();
            for ( final String xpath : managementXpaths )
            {
                final Element e = pomView.resolveXPathToElement( xpath, false );
                if ( e != null )
                {
                    managementElement = e;
                    break;
                }
            }
        }

        return managementElement;
    }

    protected List<Node> getFirstNodesWithManagement( final String path )
        throws GalleyMavenException
    {
        //        logger.info( "Resolving '%s' from node: %s", path, this.element );
        final List<Node> nodes = pomView.resolveXPathToNodeListFrom( this.element, path, true );
        if ( nodes == null || nodes.isEmpty() )
        {
            final Element managedElement = getManagementElement();
            if ( managedElement != null )
            {
                return pomView.resolveXPathToNodeListFrom( managedElement, path, true );
            }
        }

        return nodes;
    }

    private void initManagementXpaths()
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

        final List<String> xpaths = new ArrayList<>();

        final String profileId = getProfileId();
        if ( profileId != null )
        {
            final StringBuilder sb = new StringBuilder();

            sb.append( "/project/profiles/profile[id/text()=\"" )
              .append( profileId )
              .append( "\"]/" )
              .append( managementXpathFragment )
              .append( '[' )
              .append( qualifier )
              .append( "]" );

            final String xp = sb.toString();
            xpaths.add( xp );
            //            logger.info( "Created management XPath template: '%s'", xp );
        }

        final StringBuilder sb = new StringBuilder();
        sb.append( "/project/" )
          .append( managementXpathFragment )
          .append( '[' )
          .append( qualifier )
          .append( "]" );

        final String xp = sb.toString();
        xpaths.add( xp );
        //        logger.info( "Created management XPath template: '%s'", xp );

        managementXpaths = xpaths.toArray( new String[xpaths.size()] );
    }

    protected String getValue( final String path )
    {
        return getValueFrom( path, element );
    }

    protected String getValueFrom( final String path, final Element element )
    {
        String val;
        if ( path.contains( "(\\/\\/|\\[" ) )
        {
            final Element e = (Element) pomView.resolveXPathToNodeFrom( element, path, false );

            if ( e == null )
            {
                return null;
            }

            val = e.getTextContent()
                   .trim();
        }
        else
        {
            final String[] parts = path.split( "/" );
            Element e = element;
            NodeList nl;
            for ( final String part : parts )
            {
                nl = element.getElementsByTagName( part );
                if ( nl == null || nl.getLength() < 1 )
                {
                    return null;
                }

                e = (Element) nl.item( 0 );
                if ( e == null )
                {
                    return null;
                }
            }

            val = e.getTextContent();
        }

        //        logger.info( "Resolving expressions in: '%s'", val );
        return pomView.resolveExpressions( val );
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

    public String toXML()
    {
        return pomView.toXML( element );
    }

}
