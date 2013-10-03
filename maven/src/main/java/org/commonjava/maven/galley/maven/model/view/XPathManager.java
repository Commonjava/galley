package org.commonjava.maven.galley.maven.model.view;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

@ApplicationScoped
public class XPathManager
{

    private final XPath xpath;

    private final Map<String, WeakReference<XPathExpression>> xpaths = new HashMap<>();

    public XPathManager()
    {
        this.xpath = XPathFactory.newInstance()
                                 .newXPath();
    }

    public synchronized void clear()
    {
        xpaths.clear();
    }

    public XPathExpression getXPath( final String path, final boolean cache )
        throws XPathExpressionException
    {
        XPathExpression expression = null;
        if ( cache )
        {
            synchronized ( this )
            {
                final WeakReference<XPathExpression> ref = xpaths.get( path );
                if ( ref != null )
                {
                    expression = ref.get();
                }

                if ( expression == null )
                {
                    expression = xpath.compile( path );
                    xpaths.put( path, new WeakReference<XPathExpression>( expression ) );
                }
            }
        }
        else
        {
            expression = xpath.compile( path );
        }

        return expression;
    }

}
