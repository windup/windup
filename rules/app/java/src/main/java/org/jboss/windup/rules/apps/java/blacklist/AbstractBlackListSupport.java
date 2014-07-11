package org.jboss.windup.rules.apps.java.blacklist;

import org.jboss.windup.rules.apps.java.model.BlackListModel;
import org.jboss.windup.rules.apps.java.model.ClassCandidateTypeModel;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidate;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
import org.jboss.windup.rules.apps.java.scan.ast.event.JavaScannerASTEvent;

/**
 * @author jsightler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AbstractBlackListSupport implements ASTEventEvaluator
{
    private final String hint;
    private final String ruleID;
    private int effort = 0;
    private Types candidateTypes;

    public AbstractBlackListSupport(String hint, String ruleID, int effort,
                Types types)
    {
        this.hint = hint;
        this.ruleID = ruleID;
        this.effort = effort;
        this.candidateTypes = types;
    }
    
    public Types getCandidateTypes() {
        return candidateTypes;
    }

    String getRuleID()
    {
        return ruleID;
    }

    String getHint()
    {
        return hint;
    }

    public int getEffort()
    {
        return effort;
    }

    protected BlackListModel createBlackListModel(JavaScannerASTEvent event)
    {
        ClassCandidate classCandidate = event.getClassCandidate();
        BlackListModel blackListModel = event.getContext().getFramed().addVertex(null, BlackListModel.class);
        for(ClassCandidateType type: candidateTypes) {
            ClassCandidateTypeModel classCandidateModel = event.getContext().getFramed().addVertex(null,
                        ClassCandidateTypeModel.class);
            classCandidateModel.setClassCandidateType(type);
            blackListModel.addClassCandidateType(classCandidateModel);
        }
        blackListModel.setFileModel(event.getFileModel());
        blackListModel.setHint(getHint());
        blackListModel.setEffort(effort);
        blackListModel.setQualifiedName(event.getClassCandidate().getQualifiedName());
        blackListModel.setRuleID(getRuleID());
        blackListModel.setLineNumber(classCandidate.getLineNumber());
        blackListModel.setStartPosition(classCandidate.getStartPosition());
        blackListModel.setLength(classCandidate.getLength());
        return blackListModel;
    }

}
