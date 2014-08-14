package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.WhiteList;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class EjbConfig
{

    /**
     * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
     * 
     */
    public class BaseConfig extends WindupRuleProvider
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
            Configuration configuration = ConfigurationBuilder.begin()
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.*") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform(
                        Iteration.over().perform(
                        WhiteList.add()
                        )
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.persistence.*") .at(TypeReferenceLocation.NOTSPECIFIED) ) .perform(
                        Iteration.over().perform(
                        WhiteList.add()
                        )
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.persistence.Entity$") .at(TypeReferenceLocation.TYPE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "JPA Entity" ).withEffort( 0
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.EJBHome$") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 1.x/2.x - Home Interface" ).withEffort( 0
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.EJBObject$") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 1.x/2.x - Remote Interface" ).withEffort( 0
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.EntityBean$") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 1.x/2.x - Entity Bean" ).withEffort( 0
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.SessionBean$") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 1.x/2.x - Session Bean" ).withEffort( 0
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.EJBLocalHome$") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 2.x - Local Home" ).withEffort( 0
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.EJBLocalObject$") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 2.x - Local Object" ).withEffort( 0
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.MessageDrivenBean$") .at(TypeReferenceLocation.INHERITANCE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 2.x - Message Driven Bean" ).withEffort( 0
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.MessageDriven$") .at(TypeReferenceLocation.TYPE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 3.x - Message Driven Bean" ).withEffort( 2
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.Local$") .at(TypeReferenceLocation.TYPE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 3.x - Local Session Bean Interface" ).withEffort( 0
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.Remote$") .at(TypeReferenceLocation.TYPE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 3.x - Remote Session Bean Interface" ).withEffort( 2
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.Stateless$") .at(TypeReferenceLocation.TYPE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 3.x - Stateless Session Bean" ).withEffort( 0
                        ))
                        .endIteration()
                        )
                        .addRule()
                        .when(
                        JavaClass.references("javax.ejb.Stateful$") .at(TypeReferenceLocation.TYPE) ) .perform(
                        Iteration.over().perform(
                        Classification.as(
                        "EJB 3.x - Stateful Session Bean" ).withEffort( 0
                        ))
                        .endIteration()
                        );
           // @formatter:on
            return configuration;
        }
    }

}
