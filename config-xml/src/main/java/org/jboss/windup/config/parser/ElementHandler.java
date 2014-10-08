package org.jboss.windup.config.parser;

import org.jboss.windup.config.exception.ConfigurationException;
import org.w3c.dom.Element;

/**
 * Parses the given {@link Element} and returns a result, that will depend upon the underlying implementation.
 */
public interface ElementHandler<T>
{
    /**
     * Parses the provided {@link Element} with the given {@link ParserContext}.
     * 
     * See also {@link XMLRuleProviderLoader}.
     */
    public T processElement(ParserContext handlerManager, Element element) throws ConfigurationException;
}
