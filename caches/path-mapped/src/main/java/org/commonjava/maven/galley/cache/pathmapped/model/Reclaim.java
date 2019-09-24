package org.commonjava.maven.galley.cache.pathmapped.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

@Entity
@Table( name = "reclaim" )
public class Reclaim
{
    @Id
    @Column( name = "fileid" )
    private String fileId;

    @Column
    private Date deletion;

    @Column( name = "filestorage" )
    private String storage;

    public Reclaim()
    {
    }

    public Reclaim( String fileId, Date deletion, String storage )
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
        Reclaim reclaim = (Reclaim) o;
        return fileId.equals( reclaim.fileId );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( fileId );
    }
}
