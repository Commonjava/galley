package org.commonjava.maven.galley.cache.pathmapped.util;

import org.commonjava.maven.galley.cache.pathmapped.model.PathMap;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
    public static <R> List<R> getParents( PathMap pathMap,
                                             PathMapCreation<String, String, String, R> pathMapCreation )
    {
        String fileSystem = pathMap.getFileSystem();
        String parent = pathMap.getParentPath(); // e.g, /foo/bar/1.0

        LinkedList<R> l = new LinkedList<>();

        String parentPath = ROOT_DIR;

        String[] toks = parent.split( "/" ); // [][foo][bar][1.0]
        for ( String tok : toks )
        {
            if ( isBlank( tok ) )
            {
                continue;
            }
            String filename = tok + "/";
            R o = pathMapCreation.apply( fileSystem, parentPath, filename );
            l.add( o );
            if ( !parentPath.endsWith( "/" ) )
            {
                parentPath += "/";
            }
            parentPath += tok;
        }
        return l;
    }

    @FunctionalInterface
    public interface PathMapCreation<T1, T2, T3, R>
    {
        R apply( T1 fileSystem, T2 parentPath, T3 filename );
    }

    public static <R> List<R> getParentsBottomUp( PathMap pathMap,
                                                    PathMapCreation<String, String, String, R> pathMapCreation )
    {
        List<R> l = getParents( pathMap, pathMapCreation );
        Collections.reverse( l );
        return l;
    }

    public static long calculateDuration( Date date )
    {
        if ( date == null )
        {
            return 0;
        }
        Duration duration = Duration.between( LocalDateTime.now(),
                                              LocalDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault() ) );
        return Math.abs( duration.toHours() );
    }

    public static String marshall( String fileSystem, String path )
    {
        return fileSystem + ":" + path;
    }

}
