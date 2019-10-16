package org.commonjava.maven.galley.cache.pathmapped.datastax.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.commonjava.maven.galley.cache.pathmapped.model.Reclaim;

import java.util.Date;
import java.util.Objects;

@Table( name = "reclaim", readConsistency = "QUORUM", writeConsistency = "QUORUM" )
public class DtxReclaim implements Reclaim
{
    @PartitionKey
    private int partition; // use fixed partition 0 in order to run SELECT fast

    @ClusteringColumn(0)
    private Date deletion;

    @ClusteringColumn(1)
    private String fileId;

    @Column
    private String storage;

    public DtxReclaim()
    {
    }

    public DtxReclaim( String fileId, Date deletion, String storage )
    {
        this.fileId = fileId;
        this.deletion = deletion;
        this.storage = storage;
    }

    public int getPartition()
    {
        return partition;
    }

    public void setPartition( int partition )
    {
    }

    @Override
    public String getFileId()
    {
        return fileId;
    }

    public void setFileId( String fileId )
    {
        this.fileId = fileId;
    }

    @Override
    public Date getDeletion()
    {
        return deletion;
    }

    public void setDeletion( Date deletion )
    {
        this.deletion = deletion;
    }

    @Override
    public String getStorage()
    {
        return storage;
    }

    public void setStorage( String storage )
    {
        this.storage = storage;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;
        DtxReclaim that = (DtxReclaim) o;
        return partition == that.partition && deletion.equals( that.deletion ) && fileId.equals( that.fileId );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( partition, deletion, fileId );
    }

    @Override
    public String toString()
    {
        return "DtxReclaim{" + "partition=" + partition + ", deletion=" + deletion + ", fileId='" + fileId + '\''
                        + ", storage='" + storage + '\'' + '}';
    }
}
