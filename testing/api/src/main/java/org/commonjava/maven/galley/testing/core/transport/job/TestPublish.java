/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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

    public void setContent( final InputStream stream, final long length, final String contentType )
    {
        this.stream = stream;
        this.length = length;
        this.contentType = contentType;
    }
}
