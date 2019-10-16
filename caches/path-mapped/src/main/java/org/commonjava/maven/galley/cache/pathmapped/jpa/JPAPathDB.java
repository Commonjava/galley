package org.commonjava.maven.galley.cache.pathmapped.jpa;

import org.commonjava.maven.galley.cache.pathmapped.jpa.model.JpaPathKey;
import org.commonjava.maven.galley.cache.pathmapped.jpa.model.JpaPathMap;
import org.commonjava.maven.galley.cache.pathmapped.jpa.model.JpaReclaim;
import org.commonjava.maven.galley.cache.pathmapped.jpa.model.JpaReverseKey;
import org.commonjava.maven.galley.cache.pathmapped.jpa.model.JpaReverseMap;
import org.commonjava.maven.galley.cache.pathmapped.model.PathMap;
import org.commonjava.maven.galley.cache.pathmapped.model.Reclaim;
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
import java.util.HashSet;
import java.util.List;

import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.ROOT_DIR;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getFilename;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getParentPath;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getParentsBottomUp;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.marshall;

public class JPAPathDB
                implements PathDB
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final EntityManagerFactory factory;

    private final EntityManager entitymanager;

    public JPAPathDB( String persistenceUnitName )
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
                        "Select p from JpaPathMap p where p.pathKey.fileSystem=?1 and p.pathKey.parentPath=?2" )
                                   .setParameter( 1, fileSystem )
                                   .setParameter( 2, path );

        List<PathMap> list = query.getResultList();
        return list;
    }

    @Override
    public int getFileLength( String fileSystem, String path )
    {
        PathMap pathMap = findPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return pathMap.getSize();
        }
        return -1;
    }

    private JpaPathMap findPathMap( String fileSystem, String path )
    {
        return entitymanager.find( JpaPathMap.class, getPathKey( fileSystem, path ) );
    }

    @Override
    public long getFileLastModified( String fileSystem, String path )
    {
        PathMap pathMap = findPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return pathMap.getCreation().getTime();
        }
        return -1;
    }

    @Override
    public boolean exists( String fileSystem, String path )
    {
        PathMap pathMap = findPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return true;
        }
        return false;
    }

    @Override
    public void insert( String fileSystem, String path, Date date, String fileId, int size, String fileStorage )
    {
        JpaPathMap pathMap = new JpaPathMap();
        JpaPathKey pathKey = getPathKey( fileSystem, path );
        pathMap.setPathKey( pathKey );
        pathMap.setCreation( date );
        pathMap.setFileId( fileId );
        pathMap.setFileStorage( fileStorage );
        pathMap.setSize( size );
        insert( pathMap );
    }

    @Override
    public void insert( PathMap pathMap )
    {
        logger.debug( "Insert: {}", pathMap );

        String fileSystem = pathMap.getFileSystem();
        String parent = pathMap.getParentPath();

        makeDirs( fileSystem, parent );

        String path = PathUtils.normalize( parent, pathMap.getFilename() );

        // before insertion, we need to get the prev entry and check for reclaim
        JpaPathKey key = ( (JpaPathMap) pathMap ).getPathKey();
        PathMap prev = entitymanager.find( JpaPathMap.class, key );
        if ( prev != null )
        {
            delete( fileSystem, path );
        }

        // insert path mapping and reverse mapping
        transactionAnd( () -> {
            entitymanager.persist( pathMap );
        } );

        // insert reverse mapping
        addToReverseMap( pathMap.getFileId(), fileSystem, path );
    }

    private void addToReverseMap( String fileId, String fileSystem, String path )
    {
        HashSet<String> updatedPaths = new HashSet<>();
        ReverseMap reverseMap = getReverseMap( fileId );
        if ( reverseMap != null )
        {
            updatedPaths.addAll( reverseMap.getPaths() );
        }
        updatedPaths.add( marshall( fileSystem, path ) );
        ReverseMap updatedReverseMap = new JpaReverseMap( new JpaReverseKey( fileId, 0 ), updatedPaths );
        transactionAnd( () -> entitymanager.persist( updatedReverseMap ) );
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
        PathMap pathMap = findPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return pathMap.getFileId() == null;
        }
        return false;
    }

    @Override
    public boolean isFile( String fileSystem, String path )
    {
        PathMap pathMap = findPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return pathMap.getFileId() != null;
        }
        return false;
    }

    @Override
    public boolean delete( String fileSystem, String path )
    {
        PathMap pathMap = findPathMap( fileSystem, path );
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

        transactionAnd( () -> entitymanager.remove( pathMap ) );

        removeFromReverseMap( fileSystem, path, pathMap );
        return true;
    }

    private void removeFromReverseMap( String fileSystem, String path, PathMap pathMap )
    {
        String fileId = pathMap.getFileId();
        ReverseMap reverseMap = getReverseMap( fileId );
        if ( reverseMap != null )
        {
            HashSet<String> updatedPaths = new HashSet<>( reverseMap.getPaths() );
            updatedPaths.remove( marshall( fileSystem, path ) );

            if ( updatedPaths.isEmpty() )
            {
                // reclaim, but not remove from reverse table immediately (for race-detection/double-check)
                reclaim( fileId, pathMap.getFileStorage() );
            }
            else
            {
                ReverseMap updatedReverseMap = new JpaReverseMap( new JpaReverseKey( fileId, 0 ), updatedPaths );
                transactionAnd( () -> entitymanager.persist( updatedReverseMap ) );
            }
        }
        else
        {
            reclaim( fileId, pathMap.getFileStorage() );
        }
    }

    @Override
    public String getStorageFile( String fileSystem, String path )
    {
        PathMap pathMap = findPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return pathMap.getFileStorage();
        }
        return null;
    }

    @Override
    public boolean copy( String fromFileSystem, String fromPath, String toFileSystem, String toPath )
    {
        PathMap pathMap = findPathMap( fromFileSystem, fromPath );
        if ( pathMap == null )
        {
            logger.warn( "Source PathKey not found, {}:{}", fromFileSystem, fromPath );
            return false;
        }

        JpaPathKey to = getPathKey( toFileSystem, toPath );
        PathMap target = findPathMap( toFileSystem, toPath );
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
            entitymanager.persist( new JpaPathMap( to, pathMap.getFileId(), pathMap.getCreation(), pathMap.getSize(),
                                                   pathMap.getFileStorage() ) );
        } );
        return true;
    }

    @Override
    public void makeDirs( String fileSystem, String path )
    {
        logger.debug( "Make dir, fileSystem: {}, path: {}", fileSystem, path );

        if ( ROOT_DIR.equals( path ) )
        {
            return;
        }
        if ( !path.endsWith( "/" ) )
        {
            path += "/";
        }

        JpaPathMap pathMap = findPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            logger.debug( "Dir exists, {}:{}", fileSystem, path );
            return;
        }

        JpaPathKey pathKey = getPathKey( fileSystem, path );
        pathMap = new JpaPathMap();
        pathMap.setPathKey( pathKey );

        final List<JpaPathMap> parents = getParentsBottomUp( pathMap, ( fSystem, pPath, fName ) -> {
            JpaPathMap p = new JpaPathMap();
            p.setPathKey( new JpaPathKey( fSystem, pPath, fName ) );
            return p;
        } );

        List<JpaPathMap> persist = new ArrayList<>();
        persist.add( pathMap );

        logger.debug( "Get persist: {}", persist );

        for ( JpaPathMap p : parents )
        {
            JpaPathMap o = entitymanager.find( JpaPathMap.class, p.getPathKey() );
            if ( o != null )
            {
                break;
            }
            persist.add( p );
        }

        transactionAnd( () -> persist.forEach( p -> entitymanager.persist( p ) ) );
    }

    private void reclaim( String fileId, String fileStorage )
    {
        transactionAnd( () -> {
            entitymanager.persist( new JpaReclaim( fileId, new Date(), fileStorage ) );
        } );
    }

    private ReverseMap getReverseMap( String fileId )
    {
        return entitymanager.find( JpaReverseMap.class, new JpaReverseKey( fileId, 0 ) );
    }

    @Override
    public List<Reclaim> listOrphanedFiles()
    {
        Query query = entitymanager.createQuery( "Select r from Reclaim r" );
        return query.getResultList();
    }

    @Override
    public void removeFromReclaim( Reclaim reclaim )
    {
        entitymanager.remove( reclaim );
    }

    private JpaPathKey getPathKey( String fileSystem, String path )
    {
        String parentPath = getParentPath( path );
        String filename = getFilename( path );
        return new JpaPathKey( fileSystem, parentPath, filename );
    }

}
