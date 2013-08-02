package org.commonjava.maven.galley.testutil;

import java.io.InputStream;

import org.commonjava.maven.galley.spi.transport.PublishJob;

public interface TestPublishJob
    extends PublishJob
{

    void setContent( InputStream stream, long length, String contentType );

}
