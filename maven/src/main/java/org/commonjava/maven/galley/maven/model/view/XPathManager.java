package org.commonjava.maven.galley.maven.model.view;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

@ApplicationScoped
public class XPathManager
{

    public static final String G = "groupId";

    public static final String A = "artifactId";

    public static final String V = "version";

    public static final String T = "type";

    public static final String C = "classifier";

    public static final String AND = " and ";

    public static final String OR = " or ";

    public static final String NOT = " not(";

    public static final String OPEN_PAREN = "(";

    public static final String END_PAREN = ")";

    public static final String TEXTEQ = "/text()=\"";

    public static final String QUOTE = "\"";

    private final XPath xpath;

    //    private final Map<String, WeakReference<XPathExpression>> xpaths = new HashMap<>();

    public XPathManager()
    {
        this.xpath = XPathFactory.newInstance()
                                 .newXPath();
    }

    public synchronized void clear()
    {
        //        xpaths.clear();
    }

    public XPathExpression getXPath( final String path, final boolean cache )
        throws XPathExpressionException
    {
        XPathExpression expression = null;
        //        if ( cache )
        //        {
        //            synchronized ( this )
        //            {
        //                final WeakReference<XPathExpression> ref = xpaths.get( path );
        //                if ( ref != null )
        //                {
        //                    expression = ref.get();
        //                }
        //
        //                if ( expression == null )
        //                {
        //                    expression = xpath.compile( path );
        //                    xpaths.put( path, new WeakReference<XPathExpression>( expression ) );
        //                }
        //            }
        //        }
        //        else
        //        {
        expression = xpath.compile( path );
        //        }

        return expression;
    }

}
