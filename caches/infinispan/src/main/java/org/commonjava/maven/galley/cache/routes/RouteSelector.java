package org.commonjava.maven.galley.cache.routes;

import org.commonjava.maven.galley.model.ConcreteResource;

public interface RouteSelector
{
    boolean isDisposable(ConcreteResource resource);
}
