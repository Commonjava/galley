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
package org.commonjava.maven.galley.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.commonjava.maven.galley.TransferException;

public class TransferBatch
{

    private Set<Resource> resources;

    private Map<ConcreteResource, Transfer> transfers;

    private Map<ConcreteResource, TransferException> errors;

    protected TransferBatch()
    {
        this.resources = new HashSet<>();
    }

    protected void setResources( final Set<? extends Resource> resources )
    {
        this.resources = new HashSet<>( resources );
    }

    public TransferBatch( final Collection<? extends Resource> resources )
    {
        this.resources = new HashSet<>( resources );
    }

    public Set<Resource> getResources()
    {
        return resources;
    }

    public void setErrors( final Map<ConcreteResource, TransferException> errors )
    {
        this.errors = errors;
    }

    public void setTransfers( final Map<ConcreteResource, Transfer> transfers )
    {
        this.transfers = transfers;
    }

    public Map<ConcreteResource, Transfer> getTransfers()
    {
        return transfers == null ? Collections.emptyMap() : transfers;
    }

    public Transfer getTransfer( final ConcreteResource resource )
    {
        return transfers == null ? null : transfers.get( resource );
    }

    public Map<ConcreteResource, TransferException> getErrors()
    {
        return errors == null ? Collections.emptyMap() : errors;
    }

    public TransferException getError( final ConcreteResource resource )
    {
        return errors == null ? null : errors.get( resource );
    }

}
