package org.commonjava.maven.galley.model;

import static org.apache.commons.lang.StringUtils.join;

import java.util.Map;

public class Resource
{

    public static final String ROOT = "/";

    private static final String[] ROOT_ARRY = { ROOT };

    private final Location location;

    private final String path;

    public Resource( final Location location, final String... path )
    {
        this.location = location;
        this.path = normalize( path );
    }

    public static String normalize( final String... path )
    {
        if ( path == null || path.length < 1 )
        {
            return ROOT;
        }

        String result = join( path, "/" );
        while ( result.startsWith( "/" ) && result.length() > 1 )
        {
            result = result.substring( 1 );
        }

        return result;
    }

    public Location getLocation()
    {
        return location;
    }

    public String getPath()
    {
        return path;
    }

    @Override
    public String toString()
    {
        return String.format( "Resource [location=%s, path=%s]", location, path );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( location == null ) ? 0 : location.hashCode() );
        result = prime * result + ( ( path == null ) ? 0 : path.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final Resource other = (Resource) obj;
        if ( location == null )
        {
            if ( other.location != null )
            {
                return false;
            }
        }
        else if ( !location.equals( other.location ) )
        {
            return false;
        }
        if ( path == null )
        {
            if ( other.path != null )
            {
                return false;
            }
        }
        else if ( !path.equals( other.path ) )
        {
            return false;
        }
        return true;
    }

    public boolean isRoot()
    {
        return path == ROOT || ROOT.equals( path );
    }

    public Resource getParent()
    {
        if ( isRoot() )
        {
            return null;
        }

        return new Resource( location, parentPath( path ) );
    }

    public Resource getChild( final String file )
    {
        return new Resource( location, path, file );
    }

    private String[] parentPath( final String path )
    {
        final String[] parts = path.split( "/" );
        if ( parts.length == 1 )
        {
            return ROOT_ARRY;
        }
        else
        {
            final String[] parentParts = new String[parts.length - 1];
            System.arraycopy( parts, 0, parentParts, 0, parentParts.length );
            return parentParts;
        }
    }

    public boolean allowsDownloading()
    {
        return location.allowsDownloading();
    }

    public boolean allowsPublishing()
    {
        return location.allowsPublishing();
    }

    public boolean allowsStoring()
    {
        return location.allowsStoring();
    }

    public boolean allowsSnapshots()
    {
        return location.allowsSnapshots();
    }

    public boolean allowsReleases()
    {
        return location.allowsReleases();
    }

    public String getLocationUri()
    {
        return location.getUri();
    }

    public String getLocationName()
    {
        return location.getName();
    }

    public int getTimeoutSeconds()
    {
        return location.getTimeoutSeconds();
    }

    public Map<String, Object> getAttributes()
    {
        return location.getAttributes();
    }

    public <T> T getAttribute( final String key, final Class<T> type )
    {
        return location.getAttribute( key, type );
    }

    public Object removeAttribute( final String key )
    {
        return location.removeAttribute( key );
    }

    public Object setAttribute( final String key, final Object value )
    {
        return location.setAttribute( key, value );
    }
}
