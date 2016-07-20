package org.commonjava.maven.galley.cache.infinispan;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifier used to supply cache for nfs owner in {@link org.commonjava.maven.galley.cache.infinispan.FastLocalCacheProvider}
 */
@Qualifier
@Target( { ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface NFSOwnerCache
{
}
