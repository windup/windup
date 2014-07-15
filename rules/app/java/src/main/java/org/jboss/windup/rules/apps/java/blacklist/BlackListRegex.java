package org.jboss.windup.rules.apps.java.blacklist;

import java.util.regex.Pattern;

import org.jboss.windup.rules.apps.java.model.BlackListModel;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidate;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
import org.jboss.windup.rules.apps.java.scan.ast.event.JavaScannerASTEvent;

/**
 * A black list item having a regex property to search for the match. Class gathers information about black list item. If registered, it respond to all of the {@link JavaScannerASTEvent} and create 
 * a vertex of the java classification, if the {@link ClassCandidate} contained in the event contain the information stored in this instance.
 * @author mbriskar
 *
 */

public class BlackListRegex extends AbstractBlackListSupport
{
    private final Pattern regexPattern;

    public BlackListRegex(String ruleID, String hint, String regex, int effort,
                Types types)
    {
        super(hint, ruleID, effort, types);
        this.regexPattern = Pattern.compile(regex);
    }

    @Override
    public void evaluateASTEvent(JavaScannerASTEvent event)
    {
        String qualName = event.getClassCandidate().getQualifiedName();
        ClassCandidateType candidateType = event.getClassCandidate().getType();
        if (regexPattern.matcher(qualName).matches())
        {
            if (getCandidateTypes() == null || getCandidateTypes().contains(candidateType))
            {
                BlackListModel blackListModel = createBlackListModel(event);
                event.getContext().getGraph().addVertex(blackListModel);
            }

        }
    }
    
    public static ModelCreatorGraphOperation add(String ruleID, String hint, String regex, int effort,
                Types types) {
        BlackListRegex blackList = new BlackListRegex(ruleID,hint,regex,effort,types);
        return new ModelCreatorGraphOperation().add(blackList);
    }
}
