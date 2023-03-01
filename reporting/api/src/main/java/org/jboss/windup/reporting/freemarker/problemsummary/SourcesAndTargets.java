package org.jboss.windup.reporting.freemarker.problemsummary;

import java.util.Map;
import java.util.Set;

public class SourcesAndTargets {
    private final Map<String, Set<Object>> issuesBySourceTech;
    private final Map<String, Set<Object>> issuesByTargetTech;
    private final Set<String> allTargets;
    private final Set<String> allSources;

    public SourcesAndTargets(Map<String, Set<Object>> issuesBySourceTech, Map<String, Set<Object>> issuesByTargetTech, Set<String> allTargets, Set<String> allSources) {
        this.issuesBySourceTech = issuesBySourceTech;
        this.issuesByTargetTech = issuesByTargetTech;
        this.allTargets = allTargets;
        this.allSources = allSources;
    }

    public Map<String, Set<Object>> getIssuesBySourceTech() {
        return issuesBySourceTech;
    }

    public Map<String, Set<Object>> getIssuesByTargetTech() {
        return issuesByTargetTech;
    }

    public Set<String> getSourceTechs() {
        return issuesBySourceTech.keySet();
    }

    public Set<String> getTargetTechs() {
        return issuesByTargetTech.keySet();
    }
}
