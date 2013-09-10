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
        return transfers == null ? Collections.<ConcreteResource, Transfer> emptyMap() : transfers;
    }

    public Transfer getTransfer( final ConcreteResource resource )
    {
        return transfers == null ? null : transfers.get( resource );
    }

    public Map<ConcreteResource, TransferException> getErrors()
    {
        return errors == null ? Collections.<ConcreteResource, TransferException> emptyMap() : errors;
    }

    public TransferException getError( final ConcreteResource resource )
    {
        return errors == null ? null : errors.get( resource );
    }

    //    public Set<TransferBatch> splitByLocation()
    //    {
    //        final Map<Location, Set<ConcreteResource>> splits = new HashMap<>();
    //        for ( final ConcreteResource r : resources )
    //        {
    //            Set<ConcreteResource> set = splits.get( r.getLocation() );
    //            if ( set == null )
    //            {
    //                set = new HashSet<>();
    //                splits.put( r.getLocation(), set );
    //            }
    //
    //            set.add( r );
    //        }
    //
    //        final Set<TransferBatch> result = new HashSet<>( splits.size() );
    //
    //        for ( final Set<ConcreteResource> resources : splits.values() )
    //        {
    //            result.add( new TransferBatch( resources ) );
    //        }
    //
    //        return result;
    //    }

}
