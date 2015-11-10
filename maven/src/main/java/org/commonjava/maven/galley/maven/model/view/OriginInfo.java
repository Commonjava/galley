package org.commonjava.maven.galley.maven.model.view;

/**
 * Created by jdcasey on 11/9/15.
 */
public class OriginInfo
{

    private boolean inherited;

    private boolean mixin;

    public OriginInfo(){}

    public OriginInfo( boolean inherited )
    {
        this.inherited = inherited;
    }

    public OriginInfo( OriginInfo info )
    {
        this.inherited = info.isInherited();
        this.mixin = info.isMixin();
    }

    public boolean isInherited()
    {
        return inherited;
    }

    public OriginInfo setInherited( boolean inherited )
    {
        this.inherited = inherited;
        return this;
    }

    public boolean isMixin()
    {
        return mixin;
    }

    public OriginInfo setMixin( boolean mixin )
    {
        this.mixin = mixin;
        return this;
    }
}
