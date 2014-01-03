/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
