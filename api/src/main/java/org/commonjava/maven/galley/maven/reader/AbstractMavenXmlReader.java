package org.commonjava.maven.galley.maven.reader;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.view.DocRef;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class AbstractMavenXmlReader<T extends ProjectRef>
{

    //    private final Map<DocCacheKey<T>, WeakReference<DocRef<T>>> cache = new ConcurrentHashMap<>();

    protected synchronized void cache( final DocRef<T> dr )
    {
        //        cache.put( new DocCacheKey<T>( dr ), new WeakReference<DocRef<T>>( dr ) );
    }

    protected synchronized DocRef<T> getFirstCached( final T ref, final List<? extends Location> locations )
    {
        //        for ( final Location location : locations )
        //        {
        //            final DocCacheKey<ProjectRef> key = new DocCacheKey<ProjectRef>( ref, location );
        //            final WeakReference<DocRef<T>> reference = cache.get( key );
        //            if ( reference != null )
        //            {
        //                final DocRef<T> dr = reference.get();
        //                if ( dr == null )
        //                {
        //                    cache.remove( key );
        //                }
        //                else
        //                {
        //                    return dr;
        //                }
        //            }
        //        }

        return null;
    }

    protected synchronized Map<Location, DocRef<T>> getAllCached( final T ref, final List<? extends Location> locations )
    {
        final Map<Location, DocRef<T>> result = new HashMap<Location, DocRef<T>>();
        //        for ( final Location location : locations )
        //        {
        //            final DocCacheKey<ProjectRef> key = new DocCacheKey<ProjectRef>( ref, location );
        //            final WeakReference<DocRef<T>> reference = cache.get( key );
        //            if ( reference != null )
        //            {
        //                final DocRef<T> dr = reference.get();
        //                if ( dr == null )
        //                {
        //                    cache.remove( key );
        //                }
        //                else
        //                {
        //                    result.put( location, dr );
        //                }
        //            }
        //        }

        return result;
    }

    protected Document parse( final Transfer transfer )
        throws GalleyMavenException
    {
        InputStream stream = null;
        try
        {
            stream = transfer.openInputStream( false );
            return DocumentBuilderFactory.newInstance()
                                         .newDocumentBuilder()
                                         .parse( stream );
        }
        catch ( IOException | SAXException | ParserConfigurationException e )
        {
            throw new GalleyMavenException( "Failed to parse: %s. Reason: %s", e, transfer, e.getMessage() );
        }
        finally
        {
            closeQuietly( stream );
        }
    }

    //    private static final class DocCacheKey<T extends ProjectRef>
    //    {
    //        private final T ref;
    //
    //        private final Location location;
    //
    //        private DocCacheKey( final T ref, final Location location )
    //        {
    //            this.ref = ref;
    //            this.location = location;
    //        }
    //
    //        public DocCacheKey( final DocRef<T> dr )
    //        {
    //            this.ref = dr.getRef();
    //            this.location = dr.getLocation();
    //        }
    //
    //        @Override
    //        public int hashCode()
    //        {
    //            final int prime = 31;
    //            int result = 1;
    //            result = prime * result + ( ( location == null ) ? 0 : location.hashCode() );
    //            result = prime * result + ( ( ref == null ) ? 0 : ref.hashCode() );
    //            return result;
    //        }
    //
    //        @Override
    //        public boolean equals( final Object obj )
    //        {
    //            if ( this == obj )
    //            {
    //                return true;
    //            }
    //            if ( obj == null )
    //            {
    //                return false;
    //            }
    //            if ( getClass() != obj.getClass() )
    //            {
    //                return false;
    //            }
    //            @SuppressWarnings( "unchecked" )
    //            final DocCacheKey<T> other = (DocCacheKey<T>) obj;
    //            if ( location == null )
    //            {
    //                if ( other.location != null )
    //                {
    //                    return false;
    //                }
    //            }
    //            else if ( !location.equals( other.location ) )
    //            {
    //                return false;
    //            }
    //            if ( ref == null )
    //            {
    //                if ( other.ref != null )
    //                {
    //                    return false;
    //                }
    //            }
    //            else if ( !ref.equals( other.ref ) )
    //            {
    //                return false;
    //            }
    //            return true;
    //        }
    //    }
}
