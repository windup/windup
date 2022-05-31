package org.jboss.windup.rules.apps.java.reporting.freemarker.dto;

import java.util.List;
import java.util.Map;

/**
 * A data transfer object carying information about the dependencies
 * in terms of list of dependency and their relations
 *
 * @author <a href="mailto:marcorizzi82@gmail.com>Marco Rizzi</a>
 */
public class DependencyGraphItemsAndRelations {
    private final Map<String, DependencyGraphItem> items;
    private final List<DependencyGraphRelation> relations;

    public DependencyGraphItemsAndRelations(Map<String, DependencyGraphItem> items,
                                            List<DependencyGraphRelation> relations) {
        this.items = items;
        this.relations = relations;
    }

    public Map<String, DependencyGraphItem> getItems() {
        return items;
    }

    public List<DependencyGraphRelation> getRelations() {
        return relations;
    }
}
