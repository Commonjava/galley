package org.commonjava.maven.galley.cache.pathmapped.datastax.model;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.commonjava.maven.galley.cache.pathmapped.model.ReverseMap;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Table( name = "reversemap", readConsistency = "QUORUM", writeConsistency = "QUORUM" )
public class DtxReverseMap implements ReverseMap
{
    @PartitionKey
    private String fileId;

    @Column
    private HashSet<String> paths;

    public DtxReverseMap()
    {
    }

    public DtxReverseMap( String fileId, HashSet<String> paths )
    {
        this.fileId = fileId;
        this.paths = paths;
    }

    public String getFileId()
    {
        return fileId;
    }

    public void setFileId( String fileId )
    {
        this.fileId = fileId;
    }

    public Set<String> getPaths()
    {
        return paths;
    }

    public void setPaths( HashSet<String> paths )
    {
        this.paths = paths;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;
        DtxReverseMap that = (DtxReverseMap) o;
        return fileId.equals( that.fileId );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( fileId );
    }

    @Override
    public String toString()
    {
        return "DtxReverseMap{" + "fileId='" + fileId + '\'' + ", paths=" + paths + '}';
    }
}
