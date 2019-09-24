package org.commonjava.maven.galley.cache.pathmapped.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table( name = "reversemap" )
public class ReverseMap
{

    @EmbeddedId
    private ReverseKey reverseKey;

    @Column
    private String paths;

    public ReverseMap()
    {
    }

    public ReverseMap( ReverseKey reverseKey, String paths )
    {
        this.reverseKey = reverseKey;
        this.paths = paths;
    }

    public ReverseKey getReverseKey()
    {
        return reverseKey;
    }

    public void setReverseKey( ReverseKey reverseKey )
    {
        this.reverseKey = reverseKey;
    }

    public String getPaths()
    {
        return paths;
    }

    public void setPaths( String paths )
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
        ReverseMap that = (ReverseMap) o;
        return reverseKey.equals( that.reverseKey );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( reverseKey );
    }
}
