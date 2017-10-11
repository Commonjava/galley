package org.commonjava.maven.galley;

import org.commonjava.maven.galley.model.ConcreteResource;

public class TransferContentException
                extends TransferException
{

    private ConcreteResource resource;

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
