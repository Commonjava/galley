package org.commonjava.maven.galley.transport.htcli.model;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;

/**
 * Created by jdcasey on 10/21/15.
 */
public enum LocationTrustType
{
    TRUST_SELF_SIGNED( "self-signed", "trust-self-signed" )
            {
                @Override
                public TrustStrategy getTrustStrategy()
                {
                    return new TrustSelfSignedStrategy();
                }
            },

    DEFAULT( "default" )
            {
                @Override
                public TrustStrategy getTrustStrategy()
                {
                    return null;
                }
            };

    private String[] aliases;

    LocationTrustType( String... aliases )
    {
        this.aliases = aliases;
    }

    public abstract TrustStrategy getTrustStrategy();

    public static LocationTrustType getType( String named )
    {
        for ( LocationTrustType type : values() )
        {
            if ( type.name().equalsIgnoreCase( named ) )
            {
                return type;
            }

            for ( String alias : type.aliases )
            {
                if ( alias.equalsIgnoreCase( named ) )
                {
                    return type;
                }
            }
        }

        return DEFAULT;
    }
}
