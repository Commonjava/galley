/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.maven.model.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenPomElementView
        extends AbstractMavenElementView<MavenPomView>
{

    //    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final String managementXpathFragment;

    private OriginInfo originInfo;

    private String[] managementXpaths;

    private MavenPomElementView managementElement;

    public MavenPomElementView( final MavenPomView pomView, final Element element, OriginInfo originInfo )
    {
        super( pomView, element );
        this.originInfo = originInfo;
        this.managementXpathFragment = null;
    }

    public MavenPomElementView( final MavenPomView pomView, final Element element, OriginInfo originInfo,
                                final String managementXpathFragment )
    {
        super( pomView, element );
        this.originInfo = originInfo;
        this.managementXpathFragment = managementXpathFragment;
    }

    public synchronized OriginInfo getOriginInfo()
    {
        if ( originInfo == null )
        {
            originInfo = new OriginInfo();
        }

        return originInfo;
    }

    /**
     * Override this to provide the xpath fragment used to find management values for this view.
     */
    protected String getManagedViewQualifierFragment()
    {
        return null;
    }

    @SuppressWarnings( "BooleanMethodIsAlwaysInverted" )
    protected boolean containsExpression( final String value )
    {
        return xmlView.containsExpression( value );
    }

    public MavenPomView getPomView()
    {
        return xmlView;
    }

    public String getProfileId()
    {
        return xmlView.getProfileIdFor( element );
    }

    protected String getManagedValue( String named )
            throws GalleyMavenException
    {
        final MavenPomElementView mgmt = getManagementElement();
        if ( mgmt != null )
        {
            return mgmt.getValue( named );
        }

        return null;
    }

    protected String getValueWithManagement( final String named )
            throws GalleyMavenException
    {
        final String value = getValue( named );
        //        logger.info( "Value of path: '{}' local to: {} is: '{}'\nIn: {}", named, element, value, pomView.getRef() );
        if ( value == null )
        {
            return getManagedValue( named );
        }

        return value;
    }

    private synchronized MavenPomElementView getManagementElement()
            throws GalleyMavenException
    {
        if ( managementElement == null )
        {
            initManagementXpaths();
            if ( managementXpaths != null )
            {
                for ( final String xpath : managementXpaths )
                {
                    final MavenPomElementView e = xmlView.resolveXPathToElementView( xpath, false, -1 );
                    if ( e != null )
                    {
                        managementElement = e;
                        break;
                    }
                }
            }
        }

        return managementElement;
    }

    protected List<XmlNodeInfo> getFirstNodesWithManagement( final String path )
            throws GalleyMavenException
    {
        //        logger.info( "Resolving '{}' from node: {}", path, this.element );
        final List<Node> nodes = xmlView.resolveXPathToNodeListFrom( elementContext, path, true );
        List<XmlNodeInfo> nodeInfos = new ArrayList<>( nodes.size() );
        if ( nodes.isEmpty() )
        {
            final MavenPomElementView managedElement = getManagementElement();
            if ( managedElement != null )
            {
                nodeInfos = managedElement.getFirstNodesWithManagement( path );
                for ( XmlNodeInfo info : nodeInfos )
                {
                    info.setMixin( managedElement.isMixin() );
                }
            }
        }
        else
        {
            for ( Node node : nodes )
            {
                nodeInfos.add( new XmlNodeInfo( isInherited(), isMixin(), node ) );
            }
        }

        return nodeInfos;
    }

    private boolean isInherited()
    {
        return originInfo != null && originInfo.isInherited();
    }

    private boolean isMixin()
    {
        return originInfo != null && originInfo.isMixin();
    }

    protected String getManagementXpathFragment()
    {
        return managementXpathFragment;
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

        final Set<String> activeProfiles = new HashSet<>( xmlView.getActiveProfileIds() );
        activeProfiles.add( getProfileId() );

        for ( final String profileId : activeProfiles )
        {
            if ( profileId != null )
            {
                @SuppressWarnings( "StringBufferReplaceableByString" )
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
                //            logger.info( "Created management XPath template: '{}'", xp );
            }
        }

        @SuppressWarnings( "StringBufferReplaceableByString" )
        final StringBuilder sb = new StringBuilder();
        sb.append( "/project/" ).append( managementXpathFragment ).append( '[' ).append( qualifier ).append( "]" );

        final String xp = sb.toString();
        xpaths.add( xp );
        //        logger.info( "Created management XPath template: '{}'", xp );

        managementXpaths = xpaths.toArray( new String[xpaths.size()] );
    }

    @Override
    protected String getValue( final String path )
    {
        final String val = super.getValue( path );
        if ( getProfileId() == null )
        {
            return xmlView.resolveExpressions( val );
        }
        else
        {
            return xmlView.resolveExpressions( val, getProfileId() );
        }
    }

}
