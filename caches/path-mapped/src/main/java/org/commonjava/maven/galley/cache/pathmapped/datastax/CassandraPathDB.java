package org.commonjava.maven.galley.cache.pathmapped.datastax;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import org.commonjava.maven.galley.cache.pathmapped.config.PathMappedStorageConfig;
import org.commonjava.maven.galley.cache.pathmapped.datastax.model.DtxPathMap;
import org.commonjava.maven.galley.cache.pathmapped.datastax.model.DtxReclaim;
import org.commonjava.maven.galley.cache.pathmapped.datastax.model.DtxReverseMap;
import org.commonjava.maven.galley.cache.pathmapped.model.PathMap;
import org.commonjava.maven.galley.cache.pathmapped.model.Reclaim;
import org.commonjava.maven.galley.cache.pathmapped.model.ReverseMap;
import org.commonjava.maven.galley.cache.pathmapped.spi.PathDB;
import org.commonjava.maven.galley.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.commonjava.maven.galley.cache.pathmapped.util.CassandraPathDBUtils.*;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.ROOT_DIR;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getFilename;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getParentPath;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.getParentsBottomUp;
import static org.commonjava.maven.galley.cache.pathmapped.util.PathMapUtils.marshall;

public class CassandraPathDB
                implements PathDB, Closeable
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private Cluster cluster;

    private Session session;

    private Mapper<DtxPathMap> pathMapMapper;

    private Mapper<DtxReverseMap> reverseMapMapper;

    private Mapper<DtxReclaim> reclaimMapper;

    private final PathMappedStorageConfig config;

    private final String keyspace;

    public CassandraPathDB( PathMappedStorageConfig config )
    {
        this.config = config;

        String host = (String) config.getProperty( PROP_CASSANDRA_HOST );
        int port = (Integer) config.getProperty( PROP_CASSANDRA_PORT );

        cluster = Cluster.builder().withoutJMXReporting().addContactPoint( host ).withPort( port ).build();

        logger.debug( "Connecting to Cassandra, host:{}, port:{}", host, port );
        session = cluster.connect();

        keyspace = (String) config.getProperty( PROP_CASSANDRA_KEYSPACE );

        session.execute( getSchemaCreateKeyspace( keyspace ) );
        session.execute( getSchemaCreateTablePathmap( keyspace ) );
        session.execute( getSchemaCreateTableReversemap( keyspace ) );
        session.execute( getSchemaCreateTableReclaim( keyspace ) );

        MappingManager manager = new MappingManager( session );

        pathMapMapper = manager.mapper( DtxPathMap.class, keyspace );
        reverseMapMapper = manager.mapper( DtxReverseMap.class, keyspace );
        reclaimMapper = manager.mapper( DtxReclaim.class, keyspace );
    }

    @Override
    public void close()
    {
        session.close();
        cluster.close();
        logger.debug( "Connection closed" );
    }

    public Session getSession()
    {
        return session;
    }

    public List<PathMap> list( String fileSystem, String path )
    {
        if ( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }

        ResultSet result =
                        session.execute( "SELECT * FROM " + keyspace + ".pathmap WHERE filesystem=? and parentpath=?;",
                                         fileSystem, path );
        List<PathMap> list = new ArrayList<>();
        Result<DtxPathMap> ret = pathMapMapper.map( result );
        ret.all().forEach( row -> list.add( row ) );
        return list;
    }

    @Override
    public int getFileLength( String fileSystem, String path )
    {
        PathMap pathMap = getPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return pathMap.getSize();
        }
        return -1;
    }

    private DtxPathMap getPathMap( String fileSystem, String path )
    {
        String parentPath = getParentPath( path );
        String filename = getFilename( path );

        return pathMapMapper.get( fileSystem, parentPath, filename );
    }

    @Override
    public long getFileLastModified( String fileSystem, String path )
    {
        PathMap pathMap = getPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return pathMap.getCreation().getTime();
        }
        return -1;
    }

    @Override
    public boolean exists( String fileSystem, String path )
    {
        return getPathMap( fileSystem, path ) != null;
    }

    @Override
    public void insert( String fileSystem, String path, Date date, String fileId, int size, String fileStorage )
    {
        DtxPathMap pathMap = new DtxPathMap();
        pathMap.setFileSystem( fileSystem );
        String parentPath = getParentPath( path );
        String filename = getFilename( path );
        pathMap.setParentPath( parentPath );
        pathMap.setFilename( filename );
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
        PathMap prev = getPathMap( fileSystem, path );
        if ( prev != null )
        {
            delete( fileSystem, path );
        }

        pathMapMapper.save( (DtxPathMap) pathMap );

        // insert reverse mapping
        addToReverseMap( pathMap.getFileId(), marshall( fileSystem, path ) );
    }

    @Override
    public boolean isDirectory( String fileSystem, String path )
    {
        PathMap pathMap = getPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return pathMap.getFileId() == null;
        }
        return false;
    }

    @Override
    public boolean isFile( String fileSystem, String path )
    {
        PathMap pathMap = getPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return pathMap.getFileId() != null;
        }
        return false;
    }

    @Override
    public boolean delete( String fileSystem, String path )
    {
        PathMap pathMap = getPathMap( fileSystem, path );
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

        pathMapMapper.delete( pathMap.getFileSystem(), pathMap.getParentPath(), pathMap.getFilename() );

        // update reverse map
        ReverseMap reverseMap = deleteFromReverseMap( pathMap.getFileId(), marshall( fileSystem, path ) );
        if ( reverseMap == null || reverseMap.getPaths() == null || reverseMap.getPaths().isEmpty() )
        {
            // reclaim, but not remove from reverse table immediately (for race-detection/double-check)
            reclaim( fileId, pathMap.getFileStorage() );
        }
        return true;
    }

    private ReverseMap deleteFromReverseMap( String fileId, String path )
    {
        session.execute( "UPDATE " + keyspace + ".reversemap SET paths -= {'" + path + "'} WHERE fileid=?;", fileId );
        return reverseMapMapper.get( fileId );
    }

    private void addToReverseMap( String fileId, String path )
    {
        session.execute( "UPDATE " + keyspace + ".reversemap SET paths += {'" + path + "'} WHERE fileid=?;", fileId );
    }

    private void reclaim( String fileId, String fileStorage )
    {
        reclaimMapper.save( new DtxReclaim( fileId, new Date(), fileStorage ) );
    }

    @Override
    public String getStorageFile( String fileSystem, String path )
    {
        PathMap pathMap = getPathMap( fileSystem, path );
        if ( pathMap != null )
        {
            return pathMap.getFileStorage();
        }
        return null;
    }

    @Override
    public boolean copy( String fromFileSystem, String fromPath, String toFileSystem, String toPath )
    {
        PathMap pathMap = getPathMap( fromFileSystem, fromPath );
        if ( pathMap == null )
        {
            logger.warn( "Source not found, {}:{}", fromFileSystem, fromPath );
            return false;
        }

        PathMap target = getPathMap( toFileSystem, toPath );
        if ( target != null )
        {
            logger.info( "Target already exists, delete it. {}:{}", toFileSystem, toPath );
            delete( toFileSystem, toPath );
        }

        // check parent paths
        String fromParentPath = getParentPath( fromPath );
        String toParentPath = getParentPath( toPath );
        if ( !fromParentPath.equals( toParentPath ) )
        {
            makeDirs( toFileSystem, toParentPath );
        }

        String toFilename = getFilename( toPath );
        pathMapMapper.save( new DtxPathMap( toFileSystem, toParentPath, toFilename, pathMap.getFileId(),
                                            pathMap.getCreation(), pathMap.getSize(), pathMap.getFileStorage() ) );
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

        DtxPathMap pathMap = new DtxPathMap();
        pathMap.setFileSystem( fileSystem );
        pathMap.setParentPath( getParentPath( path ) );
        pathMap.setFilename( getFilename( path ) );

        final List<DtxPathMap> parents = getParentsBottomUp( pathMap, ( fSystem, pPath, fName ) -> {
            DtxPathMap p = new DtxPathMap();
            p.setFileSystem( fSystem );
            p.setParentPath( pPath );
            p.setFilename( fName );
            return p;
        } );

        List<DtxPathMap> persist = new ArrayList<>();
        persist.add( pathMap );
        persist.addAll( parents );

        logger.debug( "Persist: {}", persist );
        persist.forEach( e -> pathMapMapper.save( e ) );
    }

    @Override
    public List<Reclaim> listOrphanedFiles()
    {
        // timestamp data type is encoded as the number of milliseconds since epoch
        long threshold = getReclaimThreshold( new Date(), config.getGCGracePeriodInHours() );
        ResultSet result =
                        session.execute( "SELECT * FROM " + keyspace + ".reclaim WHERE partition = 0 AND deletion < ?;",
                                         threshold );
        List<Reclaim> list = new ArrayList<>();
        Result<DtxReclaim> ret = reclaimMapper.map( result );
        ret.all().forEach( row -> {
            list.add( row );
        } );
        return list;
    }

    @Override
    public void removeFromReclaim( Reclaim reclaim )
    {
        reclaimMapper.delete( (DtxReclaim) reclaim );
    }

    private long getReclaimThreshold( Date date, int gcGracePeriodInHours )
    {
        return date.getTime() - Duration.ofHours( gcGracePeriodInHours ).toMillis();
    }

}
