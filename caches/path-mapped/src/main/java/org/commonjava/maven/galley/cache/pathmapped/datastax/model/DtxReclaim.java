package org.commonjava.maven.galley.cache.pathmapped.datastax.model;

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
    private String fileId;

    @Column
    private Date deletion;

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

    public String getFileId()
    {
        return fileId;
    }

    public void setFileId( String fileId )
    {
        this.fileId = fileId;
    }

    public Date getDeletion()
    {
        return deletion;
    }

    public void setDeletion( Date deletion )
    {
        this.deletion = deletion;
    }

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
        DtxReclaim reclaim = (DtxReclaim) o;
        return fileId.equals( reclaim.fileId );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( fileId );
    }

    @Override
    public String toString()
    {
        return "DtxReclaim{" + "fileId='" + fileId + '\'' + ", deletion=" + deletion + ", storage='" + storage + '\''
                        + '}';
    }
}
