package org.commonjava.maven.galley.cache.pathmapped.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table( name = "filechecksum" )
public class FileChecksum
{
    @Id
    private String checksum;

    @Column( name = "fileid" )
    private String fileId;

    public FileChecksum()
    {
    }

    public FileChecksum( String checksum, String fileId )
    {
        this.checksum = checksum;
        this.fileId = fileId;
    }

    public String getFileId()
    {
        return fileId;
    }

    public void setFileId( String fileId )
    {
        this.fileId = fileId;
    }

    public String getChecksum()
    {
        return checksum;
    }

    public void setChecksum( String checksum )
    {
        this.checksum = checksum;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;
        FileChecksum that = (FileChecksum) o;
        return checksum.equals( that.checksum );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( checksum );
    }
}
