package org.commonjava.maven.galley.cache.pathmapped.core;

import org.commonjava.maven.galley.cache.pathmapped.model.PathKey;
import org.commonjava.maven.galley.cache.pathmapped.model.PathMap;
import org.commonjava.maven.galley.cache.pathmapped.model.Reclaim;
import org.commonjava.maven.galley.cache.pathmapped.model.ReverseKey;
import org.commonjava.maven.galley.cache.pathmapped.model.ReverseMap;
import org.commonjava.maven.galley.cache.pathmapped.spi.PathDB;
import org.commonjava.maven.galley.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.ROOT_DIR;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getParentsBottomUp;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getPathKey;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.parsePathKeys;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.renderPathKeys;

public class RDBMSPathDB
                implements PathDB
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final EntityManagerFactory factory;

    private final EntityManager entitymanager;

    public RDBMSPathDB( String persistenceUnitName )
    {
        factory = Persistence.createEntityManagerFactory( persistenceUnitName );
        entitymanager = factory.createEntityManager();
    }

    public List<PathMap> list( String fileSystem, String path )
    {
        if ( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }

        Query query = entitymanager.createQuery(
                        "Select p from PathMap p where p.pathKey.fileSystem=?1 and p.pathKey.parentPath=?2" )
                                   .setParameter( 1, fileSystem )
                                   .setParameter( 2, path );

        List<PathMap> list = query.getResultList();
        return list;
    }

    @Override
    public int getFileLength( String fileSystem, String path )
    {
        PathMap pathMap = entitymanager.find( PathMap.class, getPathKey( fileSystem, path ) );
        if ( pathMap != null )
        {
            return pathMap.getSize();
        }
        return -1;
    }

    @Override
    public long getFileLastModified( String fileSystem, String path )
    {
        PathMap pathMap = entitymanager.find( PathMap.class, getPathKey( fileSystem, path ) );
        if ( pathMap != null )
        {
            return pathMap.getCreation().getTime();
        }
        return -1;
    }

    @Override
    public boolean exists( String fileSystem, String path )
    {
        PathMap pathMap = entitymanager.find( PathMap.class, getPathKey( fileSystem, path ) );
        if ( pathMap != null )
        {
            return true;
        }
        return false;
    }

    @Override
    public void insert( PathMap pathMap )
    {
        logger.debug( "Insert: {}", pathMap );

        PathKey key = pathMap.getPathKey();
        String fileSystem = key.getFileSystem();
        String parent = key.getParentPath();

        makeDirs( fileSystem, parent );

        // before insertion, we need to get the prev entry and check for reclaim
        // we can thread off it in future
        // Cassandra can do it very easily by "insert ... if not exists"

        PathMap prev = entitymanager.find( PathMap.class, pathMap.getPathKey() );
        if ( prev != null )
        {
            delete( fileSystem, PathUtils.normalize( parent, key.getFilename() ) );
        }

        // insert new entry
        transactionAnd( () -> {
            entitymanager.persist( pathMap );
        } );
    }

    private void transactionAnd( Runnable job )
    {
        entitymanager.getTransaction().begin();
        job.run();
        entitymanager.getTransaction().commit();
    }

    @Override
    public boolean isDirectory( String fileSystem, String path )
    {
        PathMap pathMap = entitymanager.find( PathMap.class, getPathKey( fileSystem, path ) );
        if ( pathMap != null )
        {
            return pathMap.getFileId() == null;
        }
        return false;
    }

    @Override
    public boolean isFile( String fileSystem, String path )
    {
        PathMap pathMap = entitymanager.find( PathMap.class, getPathKey( fileSystem, path ) );
        if ( pathMap != null )
        {
            return pathMap.getFileId() != null;
        }
        return false;
    }

    @Override
    public boolean delete( String fileSystem, String path )
    {
        PathMap pathMap = entitymanager.find( PathMap.class, getPathKey( fileSystem, path ) );
        if ( pathMap == null )
        {
            logger.debug( "File not exists, {}", pathMap );
            return true;
        }

        String fileId = pathMap.getFileId();
        if ( fileId == null )
        {
            logger.debug( "Can not delete a directory, {}", pathMap );
            return false;
        }

        // delete the path map
        transactionAnd( () -> entitymanager.remove( pathMap ) );

        // update reverse map
        List<ReverseMap> l = getReverseMapList( pathMap.getFileId() );
        if ( !l.isEmpty() )
        {
            ReverseMap last = l.get( l.size() - 1 );
            String paths = last.getPaths();
            String updatedPaths = updatePaths( paths, pathMap.getPathKey() );
            if ( isBlank( updatedPaths ) )
            {
                // remove reverses and reclaim
                transactionAnd( () -> {
                    l.forEach( e -> entitymanager.remove( e ) );
                } );
                reclaim( fileId, pathMap.getFileStorage() );
            }
            else
            {
                // update reverse (insert version+1 mapping)
                int version = last.getReverseKey().getVersion() + 1;
                ReverseMap reverseMap = new ReverseMap( new ReverseKey( fileId, version ), updatedPaths );
                transactionAnd( () -> entitymanager.persist( reverseMap ) );
            }
        }
        else
        {
            reclaim( fileId, pathMap.getFileStorage() );
        }
        return true;
    }

    @Override
    public String getStorageFile( String fileSystem, String path )
    {
        PathMap pathMap = entitymanager.find( PathMap.class, getPathKey( fileSystem, path ) );
        if ( pathMap != null )
        {
            return pathMap.getFileStorage();
        }
        return null;
    }

    @Override
    public void copy( String fromFileSystem, String fromPath, String toFileSystem, String toPath )
    {
        PathKey from = getPathKey( fromFileSystem, fromPath );
        PathMap pathMap = entitymanager.find( PathMap.class, from );
        if ( pathMap == null )
        {
            logger.warn( "Source PathKey not found, {}", from );
            return;
        }

        PathKey to = getPathKey( toFileSystem, toPath );
        PathMap target = entitymanager.find( PathMap.class, to );
        if ( target != null )
        {
            logger.info( "Target PathKey already exists, delete it. {}", to );
            delete( toFileSystem, toPath );
        }

        // check parent paths
        String parentPath = to.getParentPath();
        String toParentPath = to.getParentPath();
        if ( !parentPath.equals( toParentPath ) )
        {
            makeDirs( toFileSystem, toParentPath );
        }

        transactionAnd( () -> {
            entitymanager.persist( new PathMap( to, pathMap.getFileId(), pathMap.getCreation(), pathMap.getSize(), pathMap.getFileStorage() ) );
        } );
    }

    @Override
    public void makeDirs( String fileSystem, String path )
    {
        logger.debug( "Make dir, fileSystem: {}, path: {}", fileSystem, path );

        if ( ROOT_DIR.equals( path) )
        {
            return;
        }
        if ( !path.endsWith( "/" ) )
        {
            path += "/";
        }
        PathKey pathKey = getPathKey( fileSystem, path );
        PathMap pathMap = entitymanager.find( PathMap.class, pathKey );
        if ( pathMap != null )
        {
            logger.debug( "Dir exists, {}", pathKey );
            return;
        }

        pathMap = new PathMap();
        pathMap.setPathKey( pathKey );

        final List<PathMap> parents = getParentsBottomUp( pathMap );

        List<PathMap> persist = new ArrayList<>(  );
        persist.add( pathMap );

        logger.debug( "Get persist: {}", parents );

        for ( PathMap p : parents )
        {
            PathMap o = entitymanager.find( PathMap.class, p.getPathKey() );
            if ( o != null )
            {
                break;
            }
            persist.add( p );
        }

        transactionAnd( () -> {
            for ( PathMap p : persist )
            {
                entitymanager.persist( p );
            }
        } );
    }

    private void reclaim( String fileId, String fileStorage )
    {
        transactionAnd( () -> {
            entitymanager.persist( new Reclaim( fileId, new Date(), fileStorage ) );
        } );
    }

    private String updatePaths( String paths, PathKey pathKey )
    {
        Set<PathKey> pathKeys = parsePathKeys( paths );
        pathKeys.remove( pathKey );
        if ( pathKeys.isEmpty() )
        {
            return null;
        }
        return renderPathKeys( pathKeys );
    }

    private List<ReverseMap> getReverseMapList( String fileId )
    {
        Query query = entitymanager.createQuery(
                        "Select r from ReverseMap r where r.reverseKey.fileId=?1 order by r.reverseKey.version" )
                                   .setParameter( 1, fileId );
        return query.getResultList();
    }

/*
    private Query getQuery( String fileSystem, String path )
    {
        int index = path.lastIndexOf( "/" );
        String parentPath = path.substring( 0, index );
        String filename = path.substring( index + 1 );
        Query query = entitymanager.createQuery(
                        "Select p from PathMap p where p.pathKey.fileSystem=?1 and p.pathKey.parentPath=?2 and p.pathKey.filename=?3" )
                                   .setParameter( 1, fileSystem )
                                   .setParameter( 2, parentPath )
                                   .setParameter( 3, filename );
        return query;
    }
*/
}
