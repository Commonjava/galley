/**
 * Copyright (C) 2021 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.transport.htcli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.net.MalformedURLException;

import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;
import org.commonjava.maven.galley.transport.htcli.model.ManagedRepositoryLocationDecorator;
import org.commonjava.maven.galley.transport.htcli.model.SimpleHttpLocation;
import org.commonjava.util.jhttpc.JHttpCException;
import org.commonjava.util.jhttpc.INTERNAL.conn.ConnectionManagerCache;
import org.commonjava.util.jhttpc.INTERNAL.conn.ConnectionManagerTracker;
import org.commonjava.util.jhttpc.INTERNAL.conn.SiteConnectionConfig;
import org.commonjava.util.jhttpc.model.SiteConfig;
import org.commonjava.util.jhttpc.model.SiteConfigBuilder;
import org.junit.Before;
import org.junit.Test;

public class HttpImplTest {

    private HttpLocation cxfLocation, jacksonLocation, jbosswsLocation, cxfLocationWrapper, jacksonLocationWrapper, jbosswsLocationWrapper;
    private SiteConnectionConfig cxfConnConfig, jacksonConnConfig, jbosswsConnConfig;
    private ConnectionManagerCache cache;

    @Test
    public void testCreateClientCacheIdentityPerRepository()
    {
        assertFalse( cxfLocation.getName().equals( jacksonLocation.getName() ) );
        assertEquals( PKG_TYPE_MAVEN + ":" + STORE_TYPE_REMOTE, cxfLocationWrapper.getName() );
        assertEquals( cxfLocationWrapper.getName(), jacksonLocationWrapper.getName() );
    }

    @Test
    public void testSameConnectionPoolingTrackerPerRepository()
        throws JHttpCException
    {
        ConnectionManagerTracker cxfTracker = cache.getTrackerFor( cxfConnConfig );
        ConnectionManagerTracker jacksonTracker = cache.getTrackerFor( jacksonConnConfig );
        assertSame( cxfTracker, jacksonTracker );
    }

    @Test
    public void testNotSameConnectionPoolingTrackerForRepositories()
        throws JHttpCException
    {
        ConnectionManagerTracker cxfTracker = cache.getTrackerFor( cxfConnConfig );
        ConnectionManagerTracker jbosswsTracker = cache.getTrackerFor( jbosswsConnConfig );
        assertNotSame( cxfTracker, jbosswsTracker );
    }

    @Before
    public void setUp()
        throws MalformedURLException, JHttpCException
    {
        cxfLocation = asLocation( PKG_TYPE_MAVEN, "koji-org.apache.cxf-cxf-3.1.10.redhat_1-1", "http://localhost/abc/koji-org.apache.cxf-cxf-3.1.10.redhat_1-1" );
        jacksonLocation = asLocation( PKG_TYPE_MAVEN, "koji-com.fasterxml.jackson.core-jackson-core-2.9.5.foobar_2-1", "http://localhost/abc/koji-org.apache.cxf-cxf-3.1.10.redhat_1-1" );
        jbosswsLocation = asLocation( PKG_TYPE_NPM, "koji-org.jboss.ws-jbossws-parent-1.4.2.Final_foobar_00001-1", "http://localhost/abc/koji-org.jboss.ws-jbossws-parent-1.4.2.Final_foobar_00001-1" );
        cxfLocationWrapper = new ManagedRepositoryLocationDecorator ( cxfLocation );
        jacksonLocationWrapper = new ManagedRepositoryLocationDecorator ( jacksonLocation );
        jbosswsLocationWrapper = new ManagedRepositoryLocationDecorator ( jbosswsLocation );
        cxfConnConfig = asSiteConnectionConfig ( cxfLocationWrapper );
        jacksonConnConfig = asSiteConnectionConfig( jacksonLocationWrapper );
        jbosswsConnConfig = asSiteConnectionConfig( jbosswsLocationWrapper );
        cache = new ConnectionManagerCache();
    }

    private HttpLocation asLocation ( String pkg, String artifact, String uri )
        throws MalformedURLException
    {
        String location = String.format( "%1$s:%2$s:%3$s", pkg, STORE_TYPE_REMOTE, artifact );
        return new SimpleHttpLocation( location, uri, false, false, false, false, false, null );
    }

    private SiteConnectionConfig asSiteConnectionConfig ( HttpLocation location )
    {
        SiteConfig config = new SiteConfigBuilder( location.getName(), location.getUri() ).build();
        return new SiteConnectionConfig( config );
    }

    public static final String PKG_TYPE_MAVEN = "maven";

    public static final String PKG_TYPE_NPM = "npm";

    public static final String STORE_TYPE_REMOTE = "remote";
}
