package org.jboss.windup.engine.visitor.inspector;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For all classes, apply regular expressions, and color the nodes. 
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class CustomerPackageVisitor extends AbstractGraphVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerPackageVisitor.class);

    @Inject
    private WindupContext context;
    
    @Inject
    private JavaClassDao javaClassDao;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.MIGRATION_RULES;
    }
    
    @Override
    public void run() {
        String[] packagePatterns = context.getPackagesToProfile().toArray(new String[]{});
        
        
        //add the ^[package name].* to the package name to create the regex for querying the Windup graph.
        for(int i=0; i<packagePatterns.length; i++) {
            packagePatterns[i] = "^"+packagePatterns[i] + ".*";
        }
        
        
        //for all rules....
        
        for(String packagePattern : packagePatterns) {
            for(final org.jboss.windup.graph.model.resource.JavaClass entry : javaClassDao.findByJavaClassPattern(packagePattern)) {
                visitJavaClass(entry);
            }
        }
        javaClassDao.commit();
    }
    

    @Override
    public void visitJavaClass(org.jboss.windup.graph.model.resource.JavaClass entry) {
        LOG.info("Customer Package: "+entry.getQualifiedName());
        javaClassDao.markAsCustomerPackage(entry);
    }
    
}
