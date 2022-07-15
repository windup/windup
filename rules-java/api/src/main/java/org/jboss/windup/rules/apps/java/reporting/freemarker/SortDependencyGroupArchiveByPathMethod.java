package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.windup.reporting.freemarker.FreeMarkerUtil;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.rules.apps.java.dependencyreport.DependencyReportDependencyGroupModel;
import org.jboss.windup.rules.apps.java.model.comparator.DependencyReportGroupModelComparator;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.template.TemplateModelException;

/**
 * Takes a list of  and orders them according to their path.
 * <p>
 * For example, DependencyReportDependencyGroupModel with this structure:
 *
 * <ul>
 * <li>/CProject</li>
 * <li>/BProject</li>
 * <li>/AProject</li>
 * </ul>
 * <p>
 * Will be returned as:
 *
 * <ul>
 * <li>/AProject</li>
 * <li>/BProject</li>
 * <li>/CProject</li>
 * </ul>
 */
public class SortDependencyGroupArchiveByPathMethod implements WindupFreeMarkerMethod {
    private static final String NAME = "sortDependencyGroupArchivesByPathAscending";

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes an Iterable<" + DependencyReportDependencyGroupModel.class.getSimpleName() + "> and returns them, ordered alphabetically.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument (Iterable<DependencyReportDependencyGroupModel>)");
        }
        Iterable<DependencyReportDependencyGroupModel> archiveGroups = (Iterable<DependencyReportDependencyGroupModel>) FreeMarkerUtil.freemarkerWrapperToIterable(arguments.get(0));
        List<DependencyReportDependencyGroupModel> list = new ArrayList<>();
        for (DependencyReportDependencyGroupModel group : archiveGroups) {
            list.add(group);
        }
        Collections.sort(list, new DependencyReportGroupModelComparator());
        ExecutionStatistics.get().end(NAME);
        return list;
    }
}
