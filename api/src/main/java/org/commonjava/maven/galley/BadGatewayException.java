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
package org.commonjava.maven.galley;


public class BadGatewayException
    extends TransferException
{

    private static final long serialVersionUID = 1L;

    private final int statusCode;

    public BadGatewayException( final int code, final String format, final Object... params )
    {
        super( format, params );
        this.statusCode = code;
    }

    public BadGatewayException( final int code, final String format, final Throwable error, final Object... params )
    {
        super( format, error, params );
        this.statusCode = code;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

}
