package org.jboss.windup.engine.visitor.inspector;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.JNDIReferenceDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.resource.XmlResource;

public class JNDIExtractorVisitor extends AbstractGraphVisitor
{

    @Inject
    private XmlResourceDao xmlDao;

    @Inject
    private JNDIReferenceDao jndiDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.COMPOSITION;
    }

    @Override
    public void run()
    {

        // for all JBoss web configs...
        for (XmlResource resource : xmlDao.findByRootTag("jboss-web"))
        {

        }

        // process all weblogic web configs..
        for (XmlResource resource : xmlDao.findByRootTag("weblogic-web-app"))
        {

        }

        // for all weblogic ejb configs...
        for (XmlResource resource : xmlDao.findByRootTag("weblogic-ejb-jar"))
        {

        }

        // for all orion app servers web config...
        for (XmlResource resource : xmlDao.findByRootTag("orion-web-app"))
        {

        }

        // for all oracle app servers ejb config...
        for (XmlResource resource : xmlDao.findByRootTag("orion-ejb-jar"))
        {

        }

        // for all IBM Websphere webapp bindings...
        for (XmlResource resource : xmlDao.findByRootTag("WebAppBinding"))
        {

        }

        // for all IBM Websphere ejb bindings...
        for (XmlResource resource : xmlDao.findByRootTag("EJBJarBinding"))
        {

        }
    }

}
