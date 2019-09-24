package org.commonjava.maven.galley.cache.pathmapped;

import org.commonjava.maven.galley.cache.pathmapped.model.PathKey;
import org.commonjava.maven.galley.cache.pathmapped.model.PathMap;
import org.junit.Test;

import java.util.List;

import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getFilename;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getParentPath;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getParents;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getPathKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PathMapUtilsTest
{

    @Test
    public void getParentsTest()
    {
        PathKey key = getPathKey( "http://foo.com", "/path/to/my/file.txt" );
        PathMap pathMap = new PathMap();
        pathMap.setPathKey( key );

        List<PathMap> l = getParents( pathMap );
        for ( PathMap map : l )
        {
            System.out.println( ">> " + map.getPathKey() );
        }
        assertEquals( 3, l.size() );
    }

    @Test
    public void getParentPathTest()
    {
        String parent = getParentPath( "/path/to/file.txt" );
        assertEquals( parent, "/path/to" );

        parent = getParentPath( "/path/to/" );
        assertEquals( parent, "/path" );

        parent = getParentPath( "/path" );
        assertEquals( parent, "/" );

        parent = getParentPath( "/" );
        assertNull( parent );
    }

    @Test
    public void getFilenameTest()
    {
        String filename = getFilename( "/path/to/file.txt" );
        assertEquals( filename, "file.txt" );

        filename = getFilename( "/path/to/" );
        assertEquals( filename, "to/" );

        filename = getFilename( "/path/" );
        assertEquals( filename, "path/" );

        filename = getFilename( "/" );
        assertNull( filename );
    }

}
