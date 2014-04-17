package org.commonjava.maven.galley.maven.parse;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Node;

public final class JXPathUtils
{

    private JXPathUtils()
    {
    }

    public static JXPathContext newContext( final Node node )
    {
        final JXPathContext ctx = JXPathContext.newContext( node );
        ctx.setLenient( true );
        ctx.setFunctions( new ClassFunctions( ResolveFunctions.class, "ext" ) );

        return ctx;
    }

}
