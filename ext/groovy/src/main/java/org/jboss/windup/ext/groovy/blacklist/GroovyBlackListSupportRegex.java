package org.jboss.windup.ext.groovy.blacklist;

import java.util.regex.Pattern;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.scan.model.BlackListModel;
import org.jboss.windup.rules.apps.javascanner.ast.event.JavaScannerASTEvent;

public class GroovyBlackListSupportRegex extends AbstractGroovyBlackListSupport
{
    private final Pattern regexPattern;

    public GroovyBlackListSupportRegex(GraphContext graphContext, String hint, String ruleID, String regex)
    {
        super(graphContext, hint, ruleID);
        this.regexPattern = Pattern.compile(regex);
    }

    @Override
    public void evaluateBlackList(JavaScannerASTEvent event)
    {
        String qualName = event.getClassCandidate().getQualifiedName();
        if (regexPattern.matcher(qualName).matches())
        {
            BlackListModel blackListModel = createBlackListModel(event);
        }
    }
}
