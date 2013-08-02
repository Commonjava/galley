package org.commonjava.maven.galley.testutil;

import java.io.InputStream;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.spi.transport.PublishJob;

public class TestPublish
    implements PublishJob
{

    private final TransferException error;

    private final String url;

    private final InputStream stream;

    private final long length;

    private final String contentType;

    private final Boolean result;

    public TestPublish( final String url, final TransferException error )
    {
        this.url = url;
        this.error = error;
        this.result = null;
        this.stream = null;
        this.length = -1;
        this.contentType = null;
    }

    public TestPublish( final String url, final Boolean result, final InputStream stream, final long length,
                        final String contentType )
    {
        this.url = url;
        this.result = result;
        this.stream = stream;
        this.length = length;
        this.contentType = contentType;
        this.error = null;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public Boolean call()
        throws Exception
    {
        return result;
    }

    public String getUrl()
    {
        return url;
    }

    public InputStream getStream()
    {
        return stream;
    }

    public long getLength()
    {
        return length;
    }

    public String getContentType()
    {
        return contentType;
    }

}
