package org.commonjava.maven.galley.transport.htcli.util;

import static org.apache.commons.io.IOUtils.closeQuietly;

import org.apache.http.client.methods.AbstractExecutionAwareRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public final class HttpUtil
{

    private HttpUtil()
    {
    }

    public static void cleanupResources( final CloseableHttpClient client, final HttpUriRequest request,
                                         final CloseableHttpResponse response )
    {
        if ( response != null && response.getEntity() != null )
        {
            EntityUtils.consumeQuietly( response.getEntity() );
            closeQuietly( response );
        }

        if ( request != null )
        {
            if ( request instanceof AbstractExecutionAwareRequest )
            {
                ( (AbstractExecutionAwareRequest) request ).reset();
            }
        }

        if ( client != null )
        {
            closeQuietly( client );
        }
    }

}
