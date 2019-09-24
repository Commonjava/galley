package org.commonjava.maven.galley.cache.pathmapped.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PathKey
                implements Serializable
{
    @Column( name = "filesystem", nullable = false )
    private String fileSystem; // correlates to the store key, e.g, 'maven:hosted:build_xyz'

    @Column( name = "parentpath", nullable = false )
    private String parentPath;

    @Column( nullable = false )
    private String filename;

    public PathKey()
    {
    }

    public PathKey( String fileSystem, String parentPath, String filename )
    {
        this.fileSystem = fileSystem;
        this.parentPath = parentPath;
        this.filename = filename;
    }

    public String getFileSystem()
    {
        return fileSystem;
    }

    public void setFileSystem( String fileSystem )
    {
        this.fileSystem = fileSystem;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public void setParentPath( String parentPath )
    {
        this.parentPath = parentPath;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename( String filename )
    {
        this.filename = filename;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;
        PathKey pathKey = (PathKey) o;
        return fileSystem.equals( pathKey.fileSystem ) && parentPath.equals( pathKey.parentPath ) && filename.equals(
                        pathKey.filename );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( fileSystem, parentPath, filename );
    }

    @Override
    public String toString()
    {
        return "PathKey{" + "fileSystem='" + fileSystem + '\'' + ", parentPath='" + parentPath + '\'' + ", filename='"
                        + filename + '\'' + '}';
    }


    // marshall to string and parse from string

    private static final String DELIMITER = "|";

    public String marshall()
    {
        return fileSystem + DELIMITER + parentPath + DELIMITER + filename;
    }

    public static PathKey parse( String s )
    {
        String[] toks = s.split( DELIMITER );
        if ( toks.length != 3 )
        {
            return null;
        }
        return new PathKey( toks[0], toks[1], toks[2] );
    }
}
