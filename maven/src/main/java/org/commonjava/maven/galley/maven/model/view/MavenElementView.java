package org.commonjava.maven.galley.maven.model.view;

import java.util.ArrayList;
import java.util.List;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MavenElementView
{

    protected static final String G = "groupId";

    protected static final String A = "artifactId";

    protected static final String V = "version";

    protected static final String AND = " and ";

    protected static final String TEXTEQ = "/text()=\"";

    protected static final String QUOTE = "\"";

    //    private final Logger logger = new Logger( getClass() );

    protected final Element element;

    protected final MavenPomView pomView;

    private final String managementXpathFragment;

    private String[] managementXpaths;

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
        throws GalleyMavenException
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
        String value = getValue( named );
        //        logger.info( "Value of path: '%s' local to: %s is: '%s'\nIn: %s", named, element, value, pomView.getRef() );
        if ( value == null )
        {
            final String[] managementXpaths = managementXpathsFor( named + "/text()" );
            for ( final String managementXpath : managementXpaths )
            {
                if ( managementXpath != null )
                {
                    value = pomView.resolveXPathExpression( managementXpath, false, -1 );
                    //                    logger.info( "Value of management xpath: '%s' in %s is '%s'\nIn: %s", named, element, value, pomView.getRef() );
                    if ( value != null )
                    {
                        break;
                    }
                }
            }
        }

        return value;
    }

    protected List<Node> getAggregateNodesWithManagement( final String path )
    {
        List<Node> nodes = pomView.resolveXPathToNodeListFrom( this.element, path, true );
        if ( nodes == null || nodes.isEmpty() )
        {
            final String[] xpaths = managementXpathsFor( path );
            for ( final String xpath : xpaths )
            {
                nodes = pomView.resolveXPathToAggregatedNodeList( xpath, false, -1 );
                if ( nodes != null && !nodes.isEmpty() )
                {
                    break;
                }
            }
        }

        return nodes;
    }

    protected List<Node> getFirstNodesWithManagement( final String path )
    {
        //        logger.info( "Resolving '%s' from node: %s", path, this.element );
        List<Node> nodes = pomView.resolveXPathToNodeListFrom( this.element, path, true );
        if ( nodes == null || nodes.isEmpty() )
        {
            final String[] xpaths = managementXpathsFor( path );
            for ( final String xpath : xpaths )
            {
                //                logger.info( "Resolving '%s' from POM hierarchy.", xpath );
                nodes = pomView.resolveXPathToFirstNodeList( xpath, false, -1 );
                if ( nodes != null && !nodes.isEmpty() )
                {
                    break;
                }
            }
        }

        return nodes;
    }

    private String[] managementXpathsFor( final String named )
        throws GalleyMavenException
    {
        initManagementXpaths();
        if ( managementXpaths == null )
        {
            return null;
        }

        final String[] result = new String[managementXpaths.length];
        for ( int i = 0; i < result.length; i++ )
        {
            //            logger.info( "Customizing management XPath: '%s' for: '%s'", managementXpaths[i], named );
            result[i] = String.format( managementXpaths[i], named );
        }

        return result;
    }

    private void initManagementXpaths()
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
              .append( "]/%s" );

            final String xp = sb.toString();
            xpaths.add( xp );
            //            logger.info( "Created management XPath template: '%s'", xp );
        }

        final StringBuilder sb = new StringBuilder();
        sb.append( "/project/" )
          .append( managementXpathFragment )
          .append( '[' )
          .append( qualifier )
          .append( "]/%s" );

        final String xp = sb.toString();
        xpaths.add( xp );
        //        logger.info( "Created management XPath template: '%s'", xp );

        managementXpaths = xpaths.toArray( new String[xpaths.size()] );
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

            NodeList matches = e.getElementsByTagName( named );
            if ( matches == null || matches.getLength() < 1 )
            {
                matches = e.getElementsByTagNameNS( "*", named );
            }

            if ( matches == null || matches.getLength() < 1 )
            {
                return null;
            }

            e = (Element) matches.item( 0 );
        }

        if ( e == null )
        {
            return null;
        }

        final String val = e.getTextContent()
                            .trim();
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

}
