package org.jboss.windup.rules.apps.legacy.java;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.blacklist.JavaClassification;
import org.jboss.windup.rules.apps.java.blacklist.ASTEventEvaluatorsBufferOperation;
import org.jboss.windup.rules.apps.java.blacklist.Types;
import org.jboss.windup.rules.apps.java.blacklist.WhiteListItem;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * 
 */
public class EjbBaseConfig extends WindupRuleProvider
{

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        List<WhiteListItem> items = new ArrayList<WhiteListItem>();
        List<JavaClassification> classifications = new ArrayList<JavaClassification>();

        items.add(new WhiteListItem(getID(), "javax.ejb.*"));
        items.add(new WhiteListItem(getID(), "javax.persistence.*"));
        classifications.add(new JavaClassification(getID(), "JPA Entity", "javax.persistence.Entity$", 0, Types.add(TypeReferenceLocation.TYPE)));
        classifications.add(new JavaClassification(getID(), "EJB 1.x/2.x - Home Interface", "javax.ejb.EJBHome$", 0, Types.add(TypeReferenceLocation.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "EJB 1.x/2.x - Remote Interface", "javax.ejb.EJBObject$", 0, Types.add(TypeReferenceLocation.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "EJB 1.x/2.x - Entity Bean", "javax.ejb.EntityBean$", 0, Types.add(TypeReferenceLocation.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "EJB 1.x/2.x - Session Bean", "javax.ejb.SessionBean$", 0, Types.add(TypeReferenceLocation.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "EJB 2.x - Local Home", "javax.ejb.EJBLocalHome$", 0, Types.add(TypeReferenceLocation.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "EJB 2.x - Local Object", "javax.ejb.EJBLocalObject$", 0, Types.add(TypeReferenceLocation.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "EJB 2.x - Message Driven Bean", "javax.ejb.MessageDrivenBean$", 0, Types.add(TypeReferenceLocation.INHERITANCE)));
        classifications.add(new JavaClassification(getID(), "EJB 3.x - Message Driven Bean", "javax.ejb.MessageDriven$", 2, Types.add(TypeReferenceLocation.TYPE)));
        classifications.add(new JavaClassification(getID(), "EJB 3.x - Local Session Bean Interface", "javax.ejb.Local$", 0, Types.add(TypeReferenceLocation.TYPE)));
        classifications.add(new JavaClassification(getID(), "EJB 3.x - Remote Session Bean Interface", "javax.ejb.Remote$", 2, Types.add(TypeReferenceLocation.TYPE)));
        classifications.add(new JavaClassification(getID(), "EJB 3.x - Stateless Session Bean", "javax.ejb.Stateless$", 0, Types.add(TypeReferenceLocation.TYPE)));
        classifications.add(new JavaClassification(getID(), "EJB 3.x - Stateful Session Bean", "javax.ejb.Stateful$", 0, Types.add(TypeReferenceLocation.TYPE))); 
        Configuration configuration = ConfigurationBuilder
            .begin()
            .addRule().perform(new ASTEventEvaluatorsBufferOperation().add(classifications));
        return configuration;
    }
}
