package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class PersistenceConfig extends WindupRuleProvider
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

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.Session.find") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated by Hibernate 3, moved to org.hibernate.classic -- use createQuery()" ).withEffort( 2 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.Session.iterate") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated by Hibernate 3, moved to org.hibernate.classic -- use createQuery()" ).withEffort( 2 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.Session.filter") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated by Hibernate 3, moved to org.hibernate.classic -- use createQuery()" ).withEffort( 2 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.Session.delete") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated by Hibernate 3, moved to org.hibernate.classic -- use createQuery()" ).withEffort( 2 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.Session.saveOrUpdateCopy") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated by Hibernate 3, moved to org.hibernate.classic -- use merge()" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.Session.createSQLQuery") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated by Hibernate 3, moved to org.hibernate.classic" ).withEffort( 3 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.Lifecycle") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated by Hibernate 3, moved to org.hibernate.classic" ).withEffort( 3 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.Validatable") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated by Hibernate 3, moved to org.hibernate.classic" ).withEffort( 3 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.PersistentEnum") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Removed in Hibernate 3, use UserType" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.FetchMode.EAGER") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated in Hibernate 3, use FetchMode.JOIN" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.FetchMode.LAZY") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated in Hibernate 3, use FetchMode.SELECT" ).withEffort( 1 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.+") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Replace net.sf.hibernate with org.hibernate (Hibernate 3)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.Interceptor") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Hibernate 3 adds two methods to the Interceptor interface; consider simply extending the EmptyInterceptor class rather than writing empty implementations" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.Interceptor.instantiate\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Method signature is now: instantiate(String entity)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.isUnsaved\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Renamed to isTransient()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.UserType") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Re-implement with additional methods, moved to org.hibernate.usertype" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.CompositeUserType") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Re-implement with additional methods, moved to org.hibernate.usertype" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.criterion") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Has undergone significant refactoring, be careful during migration" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("net.sf.hibernate.mapping") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Has undergone significant refactoring, be careful during migration" ).withEffort( 0 )
                    )
                    .endIteration()
                    );

        return configuration;
    }
    // @formatter:on
}