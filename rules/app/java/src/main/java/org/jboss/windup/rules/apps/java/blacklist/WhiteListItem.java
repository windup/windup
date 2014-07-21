package org.jboss.windup.rules.apps.java.blacklist;

import java.util.regex.Pattern;

import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.ClassCandidateTypeModel;
import org.jboss.windup.rules.apps.java.model.WhiteListModel;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidate;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
import org.jboss.windup.rules.apps.java.scan.ast.event.JavaScannerASTEvent;

/**
 * A white list item having a regex property to search for the match. Class gathers information about white list item. If registered, it respond to all of the {@link JavaScannerASTEvent} and create 
 * a vertex of the java classification, if the {@link ClassCandidate} contained in the event contain the information stored in this instance.
 * @author mbriskar
 *
 */
public class WhiteListItem implements ASTEventEvaluator
{
    private String ruleID;
    private Types classCandidateTypes;
    private Pattern regexPattern;

    public WhiteListItem(String ruleID,String regex, Types types) {
        this.ruleID=ruleID;
        this.regexPattern = Pattern.compile(regex);
        this.classCandidateTypes=types;
    }
    
    public WhiteListItem(String ruleID,String regex) {
        this(ruleID,regex,null);
    }
    
    @Override
    public void evaluateASTEvent(JavaScannerASTEvent event)
    {
        String qualName = event.getClassCandidate().getQualifiedName();
        ClassCandidateType candidateType = event.getClassCandidate().getType();
        if (regexPattern.matcher(qualName).matches())
        {
            if (classCandidateTypes == null || classCandidateTypes.contains(candidateType))
            {
                WhiteListModel whiteListModel = createWhiteListModel(event);
                event.getContext().getGraph().addVertex(whiteListModel);
            }

        }
    }
    
    WhiteListModel createWhiteListModel(JavaScannerASTEvent event)
    {
        WhiteListModel whiteListModel = new GraphService<WhiteListModel>(event.getContext(),WhiteListModel.class).create();
        for(ClassCandidateType type: classCandidateTypes) {
            GraphService<ClassCandidateTypeModel> graphService = new GraphService<ClassCandidateTypeModel>(event.getContext(),ClassCandidateTypeModel.class);
            ClassCandidateTypeModel alreadyExistingTypeModel = graphService.getUniqueByProperty(ClassCandidateTypeModel.PROPERTY_CLASS_CANDIDATE_TYPE, type);
            ClassCandidateTypeModel classCandidateModel;
            if(alreadyExistingTypeModel != null) {
                classCandidateModel=alreadyExistingTypeModel;
            } else {
                classCandidateModel = new GraphService<ClassCandidateTypeModel>(event.getContext(),ClassCandidateTypeModel.class).create();
            }
            classCandidateModel.setClassCandidateType(type);
            whiteListModel.addClassCandidateType(classCandidateModel);
        }
        whiteListModel.setFileModel(event.getFileModel());
        whiteListModel.setQualifiedName(event.getClassCandidate().getQualifiedName());
        whiteListModel.setRuleID(ruleID);
        return whiteListModel;
    }
    
    public static ASTEventEvaluatorsBufferOperation add(String ruleID,String regex, Types types) {
        WhiteListItem whiteList = new WhiteListItem(ruleID,regex,types);
        return new ASTEventEvaluatorsBufferOperation().add(whiteList);
    }

}
