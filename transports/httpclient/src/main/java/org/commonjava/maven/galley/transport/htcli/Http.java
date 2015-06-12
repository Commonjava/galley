/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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

import java.io.Closeable;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;

public interface Http
    extends Closeable
{

    public static final class CertEnumerator
    {
        private final KeyStore ks;
    
        public CertEnumerator( final KeyStore ks )
        {
            this.ks = ks;
        }
    
        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder();
    
            try
            {
                for ( final Enumeration<String> aliases = ks.aliases(); aliases.hasMoreElements(); )
                {
                    final String alias = aliases.nextElement();
                    final X509Certificate cert = (X509Certificate) ks.getCertificate( alias );
    
                    if ( cert != null )
                    {
                        sb.append( "\n" )
                          .append( cert.getSubjectDN() );
                    }
                }
            }
            catch ( final KeyStoreException e )
            {
                sb.append( "ERROR READING KEYSTORE" );
            }
    
            return sb.toString();
        }
    }

    CloseableHttpClient createClient( HttpLocation location )
        throws IOException;

    CloseableHttpClient createClient()
        throws IOException;

    HttpClientContext createContext( HttpLocation location );

    HttpClientContext createContext();

    void cleanup( CloseableHttpClient client, HttpUriRequest request, CloseableHttpResponse response );
    
}
