package org.jboss.windup.reporting.freemarker.problemsummary;

import freemarker.template.DefaultMapAdapter;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GetSourcesAndTargetsMethod implements WindupFreeMarkerMethod {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.isEmpty())
            throw new TemplateModelException("Method " + getMethodName() + " requires the following parameters (GraphRewrite event, ProjectModel project)");

        @SuppressWarnings("unchecked")
        Map<String, List<ProblemSummary>> problemSummariesArg = (Map<String, List<ProblemSummary>>) ((DefaultMapAdapter) arguments.get(0)).getWrappedObject();

        List<ProblemSummary> problemSummaries = problemSummariesArg
                .values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        Set<String> sourceTechs = getTechnologiesSet(problemSummaries, ProblemSummary::getSourceTechnologies);
        Set<String> targetTechs = getTechnologiesSet(problemSummaries, ProblemSummary::getTargetTechnologies);

        Map<String, Set<Object>> issuesBySourceTech = sourceTechs.stream()
                .map(st -> Map.entry(
                        st,
                        problemSummaries.stream()
                                .filter(ps -> ps.getSourceTechnologies().contains(st))
                                .map(ProblemSummary::getId)
                                .collect(Collectors.toSet())
                )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, Set<Object>> issuesByTargetTech = targetTechs.stream()
                .map(tt -> Map.entry(
                        tt,
                        problemSummaries.stream()
                                .filter(ps -> ps.getTargetTechnologies().contains(tt))
                                .map(ProblemSummary::getId)
                                .collect(Collectors.toSet())
                )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new SourcesAndTargets(issuesBySourceTech, issuesByTargetTech, sourceTechs, targetTechs);
    }

    private Set<String> getTechnologiesSet(List<ProblemSummary> problemSummaries, Function<ProblemSummary, List<String>> techType) {
        return problemSummaries.stream()
                .map(techType)
                .map(Set::copyOf)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

    }

    @Override
    public String getMethodName() {
        return WindupFreeMarkerMethod.super.getMethodName();
    }

    @Override
    public String getDescription() {
        return "Returns all the sources and targets present in the hints";
    }

    @Override
    public void setContext(GraphRewrite event) {
        WindupFreeMarkerMethod.super.setContext(event);
    }
}
