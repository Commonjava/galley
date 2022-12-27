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
package org.commonjava.maven.galley;

import org.commonjava.maven.galley.model.ConcreteResource;

public class TransferContentException
                extends TransferException
{

    private final ConcreteResource resource;

    private static final long serialVersionUID = 1L;

    public TransferContentException( final ConcreteResource resource, final String format, final Object... params )
    {
        super( format, params );
        this.resource = resource;
    }

    public TransferContentException( final ConcreteResource resource, final String format, final Throwable error,
                                     final Object... params )
    {
        super( format, error, params );
        this.resource = resource;
    }

    public ConcreteResource getResource()
    {
        return resource;
    }

}
