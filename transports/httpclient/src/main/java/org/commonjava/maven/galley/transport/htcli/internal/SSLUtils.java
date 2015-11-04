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
package org.commonjava.maven.galley.transport.htcli.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.x500.X500Principal;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.io.IOUtils.closeQuietly;

public final class SSLUtils
{

    private static final Integer DNSNAME_TYPE = 2;

    private SSLUtils()
    {
    }

    public static KeyStore readKeyAndCert( final String pemContent, final String keyPass )
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        final KeyStore ks = KeyStore.getInstance( KeyStore.getDefaultType() );
        ks.load( null );

        final CertificateFactory certFactory = CertificateFactory.getInstance( "X.509" );
        final KeyFactory keyFactory = KeyFactory.getInstance( "RSA" );

        final List<String> lines = readLines( pemContent );

        String currentHeader = null;
        final StringBuilder current = new StringBuilder();
        final Map<String, String> entries = new LinkedHashMap<String, String>();
        for ( final String line : lines )
        {
            if ( line == null )
            {
                continue;
            }

            if ( line.startsWith( "-----BEGIN" ) )
            {
                currentHeader = line.trim();
                current.setLength( 0 );
            }
            else if ( line.startsWith( "-----END" ) )
            {
                entries.put( currentHeader, current.toString() );
            }
            else
            {
                current.append( line.trim() );
            }
        }

        final List<Certificate> certs = new ArrayList<Certificate>();
        for ( int pass = 0; pass < 2; pass++ )
        {
            for ( final Map.Entry<String, String> entry : entries.entrySet() )
            {
                final String header = entry.getKey();
                final byte[] data = decodeBase64( entry.getValue() );

                if ( pass > 0 && header.contains( "BEGIN PRIVATE KEY" ) )
                {
                    final KeySpec spec = new PKCS8EncodedKeySpec( data );
                    final PrivateKey key = keyFactory.generatePrivate( spec );
                    ks.setKeyEntry( "key", key, keyPass.toCharArray(), certs.toArray( new Certificate[certs.size()] ) );
                }
                else if ( pass < 1 && header.contains( "BEGIN CERTIFICATE" ) )
                {
                    final Certificate c = certFactory.generateCertificate( new ByteArrayInputStream( data ) );

                    ks.setCertificateEntry( "certificate", c );
                    certs.add( c );
                }
            }
        }

        return ks;
    }

    public static KeyStore readCerts( final String pemContent, final String aliasPrefix )
        throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException
    {
        final KeyStore ks = KeyStore.getInstance( KeyStore.getDefaultType() );
        ks.load( null );

        final CertificateFactory certFactory = CertificateFactory.getInstance( "X.509" );

        final List<String> lines = readLines( pemContent );

        final StringBuilder current = new StringBuilder();
        final List<String> entries = new ArrayList<String>();
        for ( final String line : lines )
        {
            if ( line == null )
            {
                continue;
            }

            if ( line.startsWith( "-----BEGIN" ) )
            {
                current.setLength( 0 );
            }
            else if ( line.startsWith( "-----END" ) )
            {
                entries.add( current.toString() );
            }
            else
            {
                current.append( line.trim() );
            }
        }

        int i = 0;
        Logger logger = LoggerFactory.getLogger( SSLUtils.class );
        for ( final String entry : entries )
        {
            final byte[] data = decodeBase64( entry );

            final Certificate c = certFactory.generateCertificate( new ByteArrayInputStream( data ) );
            X509Certificate cert = (X509Certificate) c;

            Set<String> aliases = new HashSet<String>();
            aliases.add( aliasPrefix + i );

            KeyStore.TrustedCertificateEntry ksEntry = new KeyStore.TrustedCertificateEntry( cert );
            for ( String alias : aliases )
            {
                ks.setEntry( alias, ksEntry, null );
                logger.info( "Storing trusted cert under alias: {}\n  with DN: {}", alias, cert.getSubjectDN().getName() );
            }

            i++;
        }

        return ks;
    }

    private static List<String> readLines( final String content )
        throws IOException
    {
        final List<String> lines = new ArrayList<String>();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader( new InputStreamReader( new ByteArrayInputStream( content.getBytes( Charset.forName( "UTF-8" ) ) ) ) );
            String line = null;
            while ( ( line = reader.readLine() ) != null )
            {
                lines.add( line.trim() );
            }
        }
        finally
        {
            closeQuietly( reader );
        }

        return lines;
    }
}
