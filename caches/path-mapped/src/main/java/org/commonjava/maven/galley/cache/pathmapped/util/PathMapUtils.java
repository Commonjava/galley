package org.commonjava.maven.galley.cache.pathmapped.util;

import org.commonjava.maven.galley.cache.pathmapped.model.PathKey;
import org.commonjava.maven.galley.cache.pathmapped.model.PathMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class PathMapUtils
{
    public final static String ROOT_DIR = "/";

    // looks weird but it splits "/path/to/my/file" -> [/][path/][to/][my/][file]
    private final static String EMPTY_CHAR_AFTER_SLASH = "(?<=/)";

    public static String getParentPath( String path )
    {
        if ( ROOT_DIR.equals( path ) )
        {
            return null; // root have no parent path
        }

        StringBuilder sb = new StringBuilder();
        String[] toks = path.split( EMPTY_CHAR_AFTER_SLASH );
        for ( int i = 0; i < toks.length - 1; i++ )
        {
            sb.append( toks[i] );
        }
        String ret = sb.toString();
        if ( ret.endsWith( "/" ) )
        {
            ret = ret.substring( 0, ret.length() - 1 ); // remove trailing /
        }
        if ( ret.length() <= 0 )
        {
            ret = ROOT_DIR;
        }
        return ret;
    }

    public static String getFilename( String path )
    {
        if ( ROOT_DIR.equals( path ) )
        {
            return null;
        }
        String[] toks = path.split( EMPTY_CHAR_AFTER_SLASH );
        return toks[toks.length - 1];
    }

    public static PathKey getPathKey( String fileSystem, String path )
    {
        String parentPath = getParentPath( path );
        String filename = getFilename( path );
        return new PathKey( fileSystem, parentPath, filename );
    }

    public static String getStoragePathByFileId( String id )
    {
        String folder = id.substring( 0, 1 );
        String subFolder = id.substring( 1, 3 );
        String filename = id.substring( 3 );
        return folder + "/" + subFolder + "/" + filename;
    }

    /**
     * Get parents starting in top-down order.
     */
    public static List<PathMap> getParents( PathMap pathMap )
    {
        PathKey key = pathMap.getPathKey();
        String fileSystem = key.getFileSystem();
        String parent = key.getParentPath(); // e.g, /foo/bar/1.0

        LinkedList<PathMap> l = new LinkedList<>();

        String parentPath = ROOT_DIR;

        String[] toks = parent.split( "/" ); // [][foo][bar][1.0]
        for ( String tok : toks )
        {
            if ( isBlank( tok ) )
            {
                continue;
            }
            String filename = tok + "/";
            PathMap o = new PathMap();
            PathKey k = new PathKey( fileSystem, parentPath, filename );
            o.setPathKey( k );
            l.add( o );
            if ( !parentPath.endsWith( "/" ) )
            {
                parentPath += "/";
            }
            parentPath += tok;
        }
        return l;
    }

    public static List<PathMap> getParentsBottomUp( PathMap pathMap )
    {
        List<PathMap> l = getParents( pathMap );
        Collections.reverse( l );
        return l;
    }

    public static String renderPathKeys( Set<PathKey> pathKeys )
    {
        StringBuilder sb = new StringBuilder();
        pathKeys.forEach( pathKey -> sb.append( pathKey.marshall() + "," ) );
        return sb.toString();
    }

    public static Set<PathKey> parsePathKeys( String paths )
    {
        Set<PathKey> ret = new HashSet<>();
        String[] toks = paths.split( "," );
        for ( String tok : toks )
        {
            if ( !isBlank( tok ) )
            {
                ret.add( PathKey.parse( tok ) );
            }
        }
        return ret;
    }

}
