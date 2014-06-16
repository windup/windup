package org.jboss.windup.ext.groovy.blacklist;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.scan.model.BlackListModel;
import org.jboss.windup.rules.apps.java.scan.model.BlackListType;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidate;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
import org.jboss.windup.rules.apps.java.scan.ast.event.JavaScannerASTEvent;

public abstract class AbstractGroovyBlackListSupport implements GroovyBlackListSupport
{
    private final GraphContext graphContext;
    private final String hint;
    private final String ruleID;

    public AbstractGroovyBlackListSupport(GraphContext graphContext, String hint, String ruleID)
    {
        this.graphContext = graphContext;
        this.hint = hint;
        this.ruleID = ruleID;
    }

    GraphContext getGraphContext()
    {
        return graphContext;
    }

    String getRuleID()
    {
        return ruleID;
    }

    String getHint()
    {
        return hint;
    }

    BlackListModel createBlackListModel(JavaScannerASTEvent event)
    {
        ClassCandidate classCandidate = event.getClassCandidate();
        BlackListModel blackListModel = getGraphContext().getFramed().addVertex(null, BlackListModel.class);
        blackListModel.setBlackListType(getBlackListType(classCandidate.getType()));
        blackListModel.setFileModel(event.getFileModel());
        blackListModel.setHint(getHint());
        blackListModel.setQualifiedName(event.getClassCandidate().getQualifiedName());
        blackListModel.setRuleID(getRuleID());
        blackListModel.setLineNumber(classCandidate.getLineNumber());
        blackListModel.setStartPosition(classCandidate.getStartPosition());
        blackListModel.setLength(classCandidate.getLength());
        return blackListModel;
    }

    BlackListType getBlackListType(ClassCandidateType candidateType)
    {
        switch (candidateType)
        {
        case IMPORT:
            return BlackListType.IMPORT;
        case TYPE:
            return BlackListType.DEFINES_TYPE;
        case METHOD:
            return BlackListType.METHOD_CALL;
        case INHERITANCE:
            return BlackListType.EXTENDS_TYPE;
        case CONSTRUCTOR_CALL:
            return BlackListType.OTHER;
        case METHOD_CALL:
            return BlackListType.METHOD_CALL;
        case METHOD_PARAMETER:
            return BlackListType.OTHER;
        case ANNOTATION:
            return BlackListType.ANNOTATION;
        case RETURN_TYPE:
            return BlackListType.REFERENCES_TYPE;
        case INSTANCE_OF:
            return BlackListType.INSTANCE_OF_CHECK;
        case THROWS_METHOD_DECLARATION:
            return BlackListType.REFERENCES_EXCEPTION_OF_TYPE;
        case THROW_STATEMENT:
            return BlackListType.REFERENCES_EXCEPTION_OF_TYPE;
        case CATCH_EXCEPTION_STATEMENT:
            return BlackListType.REFERENCES_EXCEPTION_OF_TYPE;
        case FIELD_DECLARATION:
            return BlackListType.DECLARES_FIELD_OF_TYPE;
        case VARIABLE_DECLARATION:
            return BlackListType.DECLARES_VARIABLE_OF_TYPE;
        case IMPLEMENTS_TYPE:
            return BlackListType.IMPLEMENTS_TYPE;
        case EXTENDS_TYPE:
            return BlackListType.EXTENDS_TYPE;
        default:
            return BlackListType.OTHER;
        }
    }

}
