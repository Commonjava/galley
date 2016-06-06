package org.commonjava.maven.galley.transport.htcli.internal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class HttpListingTest
{

    /**
     * Tests processing of href value "http:/", which was found in listing of
     * {@link http://repo.spring.io/libs-snapshot/}.
     */
    @Test
    public void testIsSameServerHttpSlash() throws MalformedURLException
    {
        assertThat( HttpListing.isSameServer( new URL( "http://1.2.3.4/path/" ), "http:/" ),
                                              equalTo( true ) );
    }

    /**
     * Tests processing of href value "http:/", which was found in listing of
     * {@link http://repo.spring.io/libs-snapshot/}.
     */
    @Test
    public void testIsSubpathHttpSlash() throws MalformedURLException
    {
        assertThat( HttpListing.isSubpath( new URL( "http://1.2.3.4/path/" ), "http:/" ),
                                              equalTo( false ) );
    }

}
