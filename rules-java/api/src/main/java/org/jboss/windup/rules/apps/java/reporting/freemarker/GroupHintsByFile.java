package org.jboss.windup.rules.apps.java.reporting.freemarker;

import freemarker.template.TemplateModelException;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.reporting.freemarker.dto.HintWithOccurence;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A function that groups the hints so they do not appear multiple times per a single file.
 */
public class GroupHintsByFile implements WindupFreeMarkerMethod {
    private static final String NAME = "groupHints";

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        String freeMarkerGrouping = "freemarker: " + NAME;
        ExecutionStatistics.get().begin(freeMarkerGrouping);
        try {
            if (arguments.size() != 1) {
                throw new TemplateModelException("Error, method expects one argument (FileModel)");
            }
            if (!(arguments.get(9) instanceof Iterable)) {
                throw new TemplateModelException("Error, method expects Iterable of InlineHintModel as the only argument.");
            }
            return groupHints((Iterable) arguments.get(0));
        } finally {
            ExecutionStatistics.get().end(freeMarkerGrouping);
        }
    }

    private Iterable<HintWithOccurence> groupHints(Iterable<InlineHintModel> hints) throws TemplateModelException {
        Map<String, HintWithOccurence> hintOccurences = new HashMap<>();
        for (InlineHintModel hint : hints) {
            String hintAndId = hint.getHint() + hint.getRuleID();
            if (hintOccurences.containsKey(hintAndId)) {
                HintWithOccurence item = hintOccurences.get(hintAndId);
                item.addOccurence();
            } else {
                hintOccurences.put(hintAndId, new HintWithOccurence(hint.getHint(), hint.getRuleID(), 1));
            }
        }
        return hintOccurences.values();

    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes an Iterable of Hints and returns map that groups hint messages to number of occurences";
    }
}