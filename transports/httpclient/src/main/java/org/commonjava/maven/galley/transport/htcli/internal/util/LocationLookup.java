package org.commonjava.maven.galley.transport.htcli.internal.util;

import org.commonjava.maven.galley.model.Location;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jdcasey on 11/5/15.
 */
public class LocationLookup
{
    private Map<String, WeakReference<Location>> locations = new HashMap<String, WeakReference<Location>>();

    public void register( Location location )
    {
        locations.put( location.getName(), new WeakReference<Location>( location ) );
    }

    public void deregister( Location location )
    {
        locations.remove( location.getName() );
    }

    public Location lookup( String name )
    {
        WeakReference<Location> ref = locations.get( name );
        return ref == null ? null : ref.get();
    }
}
