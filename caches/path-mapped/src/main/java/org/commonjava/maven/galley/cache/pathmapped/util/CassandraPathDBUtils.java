package org.commonjava.maven.galley.cache.pathmapped.util;

public class CassandraPathDBUtils
{
    public static final String PROP_CASSANDRA_HOST = "cassandra_host";

    public static final String PROP_CASSANDRA_PORT = "cassandra_port";

    public static final String PROP_CASSANDRA_KEYSPACE = "cassandra_keyspace";

    public static String getSchema_CREATE_KEYSPACE( String keyspace )
    {
        return "CREATE KEYSPACE IF NOT EXISTS " + keyspace
                        + " WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1};";
    }

    public static String getSchema_CREATE_TABLE_PATHMAP( String keyspace )
    {
        return "CREATE TABLE IF NOT EXISTS " + keyspace + ".pathmap ("
                        + "filesystem varchar,"
                        + "parentpath varchar,"
                        + "filename varchar,"
                        + "fileid varchar,"
                        + "creation timestamp,"
                        + "size int,"
                        + "filestorage varchar,"
                        + "PRIMARY KEY (filesystem, parentpath, filename)"
                        + ");";
    }

    public static String getSchema_CREATE_TABLE_REVERSEMAP( String keyspace )
    {
        return "CREATE TABLE IF NOT EXISTS " + keyspace + ".reversemap ("
                        + "fileid varchar,"
                        + "paths set<text>,"
                        + "PRIMARY KEY (fileid)"
                        + ");";
    }

    public static String getSchema_CREATE_TABLE_RECLAIM( String keyspace )
    {
        return "CREATE TABLE IF NOT EXISTS " + keyspace + ".reclaim ("
                        + "fileid varchar,"
                        + "deletion timestamp,"
                        + "storage varchar,"
                        + "PRIMARY KEY (fileid)"
                        + ");";
    }
}
