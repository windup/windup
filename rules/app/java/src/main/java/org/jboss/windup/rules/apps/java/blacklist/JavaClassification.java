package org.jboss.windup.rules.apps.java.blacklist;

import java.util.regex.Pattern;

import javax.inject.Inject;

import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.ClassCandidateTypeModel;
import org.jboss.windup.rules.apps.java.model.ClassificationModel;
import org.jboss.windup.rules.apps.java.model.LinkDecoratorModel;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidate;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
import org.jboss.windup.rules.apps.java.scan.ast.event.JavaScannerASTEvent;
import org.jboss.windup.rules.apps.java.service.JavaClassService;

/**
 * A class collects information about java classifications. If registered, it respond to all of the {@link JavaScannerASTEvent} and create 
 * a vertex of the java classification, if the {@link ClassCandidate} contained in the event contain the information stored in this instance.
 * @author mbriskar
 *
 */
public class JavaClassification implements ASTEventEvaluator
{
    @Inject
    JavaClassService classService;
    
    private final String ruleID;
    private int effort = 0;
    private Types classCandidateTypes;
    private String description;
    private Pattern regexPattern;
    private Link[] links;

    public JavaClassification(String ruleID, String description, String regex, int effort,
                Types types, Link... links)
    {
        this.ruleID=ruleID;
        this.setDescription(description);
        this.setRegexPattern(regex);
        this.classCandidateTypes=types;
        this.links=links;
        this.effort=effort;
    }
    
    String getRuleID()
    {
        return ruleID;
    }


    public int getEffort()
    {
        return effort;
    }

    /**
     * Create a vertex in the database containing the information in the event
     * @param event
     * @return
     */
    ClassificationModel createClassificationModel(JavaScannerASTEvent event)
    {
        ClassCandidate classCandidate = event.getClassCandidate();
        ClassificationModel classificationModel = new GraphService<ClassificationModel>(event.getContext(),ClassificationModel.class).create();
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
            classificationModel.addClassCandidateType(classCandidateModel);
        }
        classificationModel.setEffort(effort);
        classificationModel.setReferencedJavaClassModel(classService.getUniqueByName(classCandidate.getQualifiedName()));
        if (links != null)
        {
            for (Link link : links)
            {
                LinkDecoratorModel linkDecorator = event.getContext().getFramed().addVertex(null,
                            LinkDecoratorModel.class);
                
                linkDecorator.setDescription(link.getDescription());
                linkDecorator.setLink(link.getLink());
                classificationModel.addLinkDecorator(linkDecorator);
            }
        }
        classificationModel.setQualifiedName(event.getClassCandidate().getQualifiedName());
        classificationModel.setRuleID(getRuleID());
        return classificationModel;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setRegexPattern(String regexPattern)
    {
        this.regexPattern = Pattern.compile(regexPattern);
    }

    /**
     * Checks if the event contains the same information as this instance. If so, create a vertex in the database.
     * @param event
     * @return
     */
    @Override
    public void evaluateASTEvent(JavaScannerASTEvent event)
    {
        String qualName = event.getClassCandidate().getQualifiedName();
        ClassCandidateType candidateType = event.getClassCandidate().getType();
        if (regexPattern.matcher(qualName).matches())
        {
            if (classCandidateTypes == null || classCandidateTypes.contains(candidateType))
            {
                ClassificationModel classificationModel = createClassificationModel(event);
                event.getContext().getGraph().addVertex(classificationModel);
            }

        }
        
    }
    
    public static ModelCreatorGraphOperation add(String ruleID, String description, String regex, int effort,
                Types types, Link... links) {
        JavaClassification classification = new JavaClassification(ruleID,description,regex,effort,types,links);
        return new ModelCreatorGraphOperation().add(classification);
    }
    

}
