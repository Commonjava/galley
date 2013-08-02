package org.commonjava.maven.galley.testutil;

import org.commonjava.maven.galley.model.Location;

public class TestEndpoint
{

    //    private final Logger logger = new Logger( getClass() );

    private final Location location;

    private final String path;

    public TestEndpoint( final Location location, final String path )
    {
        this.location = location;
        this.path = path.length() > 1 && path.startsWith( "/" ) ? path.substring( 1 ) : path;
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
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( location == null ) ? 0 : location.hashCode() );
        result = prime * result + ( ( path == null ) ? 0 : path.hashCode() );
        //        logger.info( "Hashcode for: %s is: %s", this, result );
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
        final TestEndpoint other = (TestEndpoint) obj;
        //        logger.info( "Comparing vs: %s", other );
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

    @Override
    public String toString()
    {
        return String.format( "TestEndpoint [location=%s, path=%s]", location, path );
    }

}
