package org.commonjava.maven.galley.cache.pathmapped.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ReverseKey
                implements Serializable
{
    @Column( name = "fileid", nullable = false )
    private String fileId;

    @Column( nullable = false )
    private int version;

    public ReverseKey()
    {
    }

    public ReverseKey( String fileId, int version )
    {
        this.fileId = fileId;
        this.version = version;
    }

    public String getFileId()
    {
        return fileId;
    }

    public void setFileId( String fileId )
    {
        this.fileId = fileId;
    }

    public int getVersion()
    {
        return version;
    }

    public void setVersion( int version )
    {
        this.version = version;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( o == null || getClass() != o.getClass() )
            return false;
        ReverseKey that = (ReverseKey) o;
        return version == that.version && fileId.equals( that.fileId );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( fileId, version );
    }
}
