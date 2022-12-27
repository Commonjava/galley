/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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
package org.commonjava.maven.galley.testing.core.transport.job;

import java.io.InputStream;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.spi.transport.PublishJob;

public class TestPublish
    implements PublishJob
{

    private final TransferException error;

    private final String url;

    private InputStream stream;

    private Long length;

    private String contentType;

    private final Boolean result;

    public TestPublish( final String url, final TransferException error, final Boolean result )
    {
        this.url = url;
        this.error = error;
        this.result = result;
    }

    @Override
    public TransferException getError()
    {
        return error;
    }

    @Override
    public TestPublish call()
    {
        return this;
    }

    @Override
    public long getTransferSize()
    {
        return 1;
    }

    @Override
    public boolean isSuccessful()
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

    public void setContent( final InputStream stream, final long length, final String contentType )
    {
        this.stream = stream;
        this.length = length;
        this.contentType = contentType;
    }
}
