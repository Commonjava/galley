package org.commonjava.maven.galley.transport.htcli.util;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * Created by jdcasey on 10/28/15.
 */
public final class CertEnumerator
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
                    sb.append( "\n" ).append( cert.getSubjectDN() );
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
