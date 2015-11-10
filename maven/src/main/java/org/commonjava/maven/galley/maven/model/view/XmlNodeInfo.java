package org.commonjava.maven.galley.maven.model.view;

import org.w3c.dom.Node;

/**
 * Created by jdcasey on 11/9/15.
 */
public class XmlNodeInfo
{
    private boolean inherited;
    private boolean mixin;

    private Node node;

    public XmlNodeInfo( boolean inherited, boolean mixin, Node node )
    {
        this.inherited = inherited;
        this.mixin = mixin;
        this.node = node;
    }

    public XmlNodeInfo( boolean inherited, Node node )
    {
        this( inherited, false, node );
    }

    public boolean isInherited()
    {
        return inherited;
    }

    public void setInherited( boolean inherited )
    {
        this.inherited = inherited;
    }

    public boolean isMixin()
    {
        return mixin;
    }

    public void setMixin( boolean mixin )
    {
        this.mixin = mixin;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode( Node node )
    {
        this.node = node;
    }

    public OriginInfo getOriginInfo()
    {
        return new OriginInfo( inherited ).setMixin( mixin );
    }
}
