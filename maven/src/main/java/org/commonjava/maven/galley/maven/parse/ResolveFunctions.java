/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
