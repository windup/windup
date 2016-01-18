package org.jboss.windup.rules.apps.xml.condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

class XmlFileFunctionResolver implements XPathFunctionResolver
{
    private final XPathFunctionResolver originalResolver;
    private final Map<QName, XPathFunction> functionMap = new HashMap<>();

    public XmlFileFunctionResolver(XPathFunctionResolver originalResolver)
    {
        this.originalResolver = originalResolver;
    }

    public void registerFunction(String namespaceURI, String functionName, XPathFunction function)
    {
        QName qname = new QName(namespaceURI, functionName);
        functionMap.put(qname, function);
    }

    public void clearRegisteredFunctions()
    {
        functionMap.clear();
    }

    @Override
    public XPathFunction resolveFunction(final QName functionName, final int arity)
    {
        if (functionMap.containsKey(functionName))
        {
            return new XPathFunction()
            {
                @Override
                public Object evaluate(List args) throws XPathFunctionException
                {
                    return functionMap.get(functionName).evaluate(args);
                }
            };
        }
        return originalResolver.resolveFunction(functionName, arity);
    }
}
