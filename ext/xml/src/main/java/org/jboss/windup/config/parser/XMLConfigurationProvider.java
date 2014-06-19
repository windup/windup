/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.parser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;

import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class XMLConfigurationProvider extends WindupConfigurationProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public boolean handles(Object payload)
    {
        return payload instanceof GraphContext;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        try
        {
            ClassLoader classloader = this.getClass().getClassLoader();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // FIXME This needs a more comprehensive XML location strategy
            Document doc = dBuilder.parse(classloader.getResourceAsStream("META-INF/windup-rewrite-xml-config.xml"));

            ConfigurationBuilder builder = ConfigurationBuilder.begin();
            ParserContext parser = new ParserContext(builder);

            parser.processElement(doc.getDocumentElement());

            return builder;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to parse XML configuration (better message please)", e);
        }
    }

}
