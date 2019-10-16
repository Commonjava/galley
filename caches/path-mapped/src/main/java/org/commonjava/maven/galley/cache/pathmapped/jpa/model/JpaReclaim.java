package org.commonjava.maven.galley.cache.pathmapped.jpa.model;

import org.commonjava.maven.galley.cache.pathmapped.model.Reclaim;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.Objects;

@Entity
@Table( name = "reclaim" )
public class JpaReclaim implements Reclaim
{
    @Id
    @Column( name = "fileid" )
    private String fileId;

    @Column
    private Date deletion;

    @Column
    private String storage;

    public JpaReclaim()
    {
    }

    public JpaReclaim( String fileId, Date deletion, String storage )
    {
        this.fileId = fileId;
        this.deletion = deletion;
        this.storage = storage;
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
        JpaReclaim reclaim = (JpaReclaim) o;
        return fileId.equals( reclaim.fileId );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( fileId );
    }
}
