package org.jboss.windup.ext.java.events;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.jboss.windup.rules.apps.java.blacklist.ASTEventEvaluator;
import org.jboss.windup.rules.apps.java.scan.ast.event.JavaScannerASTEvent;


/**
 * Registers the {@link ASTEventEvaluator} classes firstly, then after each {@link JavaScannerASTEvent} event informs all of them about the event.
 * @author mbriskar
 * @author jsightler
 *
 */
@Singleton
public class JavaASTEventService
{
    private static List<ASTEventEvaluator> astEventEvaluators = new ArrayList<>();

    public void onJavaScannerASTEvent(@Observes JavaScannerASTEvent event)
    {

        for (ASTEventEvaluator evaluator : astEventEvaluators)
        {
            evaluator.evaluateASTEvent(event);
        }
    }
    
    /*
     * Used in the java legacy hint rules. 
     */
    public void registerInterest(ASTEventEvaluator blacklistItem)
    {
        astEventEvaluators.add(blacklistItem);
    }

}
