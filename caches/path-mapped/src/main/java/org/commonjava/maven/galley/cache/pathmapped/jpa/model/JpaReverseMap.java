package org.commonjava.maven.galley.cache.pathmapped.jpa.model;

import org.commonjava.maven.galley.cache.pathmapped.model.ReverseMap;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table( name = "reversemap" )
public class JpaReverseMap implements ReverseMap
{

    @EmbeddedId
    private JpaReverseKey reverseKey;

    @Column
    private HashSet<String> paths;

    public JpaReverseMap()
    {
    }

    public JpaReverseMap( JpaReverseKey reverseKey, HashSet<String> paths )
    {
        this.reverseKey = reverseKey;
        this.paths = paths;
    }

    public JpaReverseKey getReverseKey()
    {
        return reverseKey;
    }

    public void setReverseKey( JpaReverseKey reverseKey )
    {
        this.reverseKey = reverseKey;
    }

    @Override
    public String getFileId()
    {
        return reverseKey.getFileId();
    }

    @Override
    public int getVersion()
    {
        return reverseKey.getVersion();
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
        JpaReverseMap that = (JpaReverseMap) o;
        return reverseKey.equals( that.reverseKey );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( reverseKey );
    }
}
