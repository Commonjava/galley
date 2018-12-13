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

import static org.commonjava.maven.galley.maven.model.view.XPathManager.V;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class MavenGAVView
        extends MavenGAView
        implements ProjectVersionRefView
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private String rawVersion;

    private String managedVersion;

    private boolean versionLookupFinished;

    public MavenGAVView( final MavenPomView pomView, final Element element, final OriginInfo originInfo, final String managementXpathFragment )
    {
        super( pomView, element, originInfo, managementXpathFragment );
    }

    public MavenGAVView( final MavenPomView pomView, final Element element, final OriginInfo originInfo )
    {
        this( pomView, element, originInfo, null );
    }

    @Override
    public String getVersion()
            throws GalleyMavenException
    {
        lookupVersion();

        return rawVersion == null ? managedVersion : rawVersion;
    }

    private synchronized void lookupVersion()
            throws GalleyMavenException
    {
        if ( !versionLookupFinished && ( rawVersion == null || managedVersion == null ) )
        {
            versionLookupFinished = true;

            //            final Logger logger = LoggerFactory.getLogger( getClass() );
            //            logger.info( "Resolving version for: {}[{}:{}]\nIn: {}", getClass().getSimpleName(), getGroupId(), getArtifactId(), pomView.getRef() );
            rawVersion = getValue( V );
            if ( getManagementXpathFragment() != null )
            {
                managedVersion = getManagedValue( V );
            }
        }
    }

    public String getRawVersion()
            throws GalleyMavenException
    {
        lookupVersion();
        return rawVersion;
    }

    public String getManagedVersion()
            throws GalleyMavenException
    {
        if ( getManagementXpathFragment() != null )
        {
            lookupVersion();
            return managedVersion;
        }

        return null;
    }

    @Override
    public ProjectVersionRef asProjectVersionRef()
            throws GalleyMavenException
    {
        try
        {
            return new SimpleProjectVersionRef( getGroupId(), getArtifactId(), getVersion() );
        }
        catch ( final IllegalArgumentException e )
        {
            throw new GalleyMavenException( "Cannot render ProjectVersionRef: {}:{}:{}. Reason: {}", e, getGroupId(),
                                            getArtifactId(), getVersion(), e.getMessage() );
        }
    }

    protected void setVersion( final String version )
    {
        setRawVersion( version );
    }

    protected void setRawVersion( String version )
    {
        this.rawVersion = version;
    }

    protected void setManagedVersion( String version )
    {
        this.managedVersion = version;
    }

    protected void setVersionLookupDone( boolean done )
    {
        this.versionLookupFinished = done;
    }

    @Override
    public String toString()
    {
        String v = rawVersion;
        if ( v == null )
        {
            v = managedVersion;
        }

        return String.format( "%s [%s:%s:%s]%s", getClass().getSimpleName(), getGroupId(), getArtifactId(),
                              v == null ? "unresolved" : v,
                              managedVersion == null ? "" : " (managed from: " + managedVersion + ")" );
    }

    @Override
    public boolean isValid()
    {
        try
        {
            return super.isValid() && !containsExpression( getVersion() );
        }
        catch ( final GalleyMavenException e )
        {
            logger.warn( "Failed to lookupVersion management element. Reason: {}", e, e.getMessage() );
        }

        return false;
    }

}
