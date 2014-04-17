package org.commonjava.maven.galley.maven.parse;

import javax.xml.xpath.XPathFunctionException;

import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResolveFunctions
{

    private static InheritableThreadLocal<MavenPomView> pomView = new InheritableThreadLocal<MavenPomView>();

    public static void setPomView( final MavenPomView pom )
    {
        pomView.set( pom );
    }

    public static MavenPomView getPomView()
    {
        return pomView.get();
    }

    public static String resolve( final String expr )
        throws XPathFunctionException
    {
        if ( expr == null || expr.trim()
                                 .length() < 1 )
        {
            return null;
        }

        final Logger logger = LoggerFactory.getLogger( ResolveFunctions.class );

        final MavenPomView pom = getPomView();

        //        logger.info( "FUNC: resolving: '{}' with pom: {}", expr, pom );

        final String result = pom.resolveExpressions( expr );

        //        logger.info( "FUNC: resolve result: '{}' with pom: {}", result, pom );

        return result;
    }
}
