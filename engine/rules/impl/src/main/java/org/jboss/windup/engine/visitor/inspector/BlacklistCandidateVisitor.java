package org.jboss.windup.engine.visitor.inspector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.JavaClassModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For all classes, apply regular expressions, and color the nodes. 
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class BlacklistCandidateVisitor extends AbstractGraphVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(BlacklistCandidateVisitor.class);

    @Inject
    private JavaClassDao javaClassDao;
    
    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.MIGRATION_RULES;
    }
    
    @Override
    public void run() {
        Collection<Pattern> patterns = new LinkedList<Pattern>();
        patterns.add(Pattern.compile(".*ejb.*"));
        
        //for all rules....
        
        for(Pattern p : patterns) {
            for(final org.jboss.windup.graph.model.resource.JavaClassModel entry : javaClassDao.findByJavaClassPattern(p.pattern())) {
                visitJavaClass(entry);
            }
        }
        javaClassDao.commit();
        
        for(JavaClassModel clz : javaClassDao.findCandidateBlacklistClasses()) {
            LOG.info("Leverages Blacklist: "+clz.getQualifiedName());
            for(JavaClassModel p : clz.providesForJavaClass()) {
                LOG.info(" -- Provides for: "+p.getQualifiedName());
            }
        }
        
        for(JavaClassModel clz : javaClassDao.findClassesLeveragingCandidateBlacklists()) {
            LOG.info("With Candidate: "+clz.getQualifiedName());
        }
    }
    

    @Override
    public void visitJavaClass(org.jboss.windup.graph.model.resource.JavaClassModel entry) {
        LOG.info("Blacklisting: "+entry.getQualifiedName());
        javaClassDao.markAsBlacklistCandidate(entry);
    }
    
}
