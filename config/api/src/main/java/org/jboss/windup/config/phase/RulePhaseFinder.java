package org.jboss.windup.config.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.windup.config.furnace.FurnaceHolder;

/**
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class RulePhaseFinder {

    private Map<String, Class<? extends RulePhase>> cachedPhases;


    private String classNameToMapKey(String className) {
        return className.toUpperCase();
    }

    /**
     * Finds the phase by its simple class name (case insensitive); returns null if not found.
     */
    public Class<? extends RulePhase> findPhase(String phaseStr) {
        if (phaseStr == null)
            return null;
        if (this.cachedPhases == null)
            this.cachedPhases = this.loadPhases();

        return this.cachedPhases.get(classNameToMapKey(phaseStr));
    }

    /**
     * Loads the currently known phases from Furnace to the map.
     */
    private Map<String, Class<? extends RulePhase>> loadPhases() {
        Map<String, Class<? extends RulePhase>> phases;
        phases = new HashMap<>();
        Furnace furnace = FurnaceHolder.getFurnace();
        for (RulePhase phase : furnace.getAddonRegistry().getServices(RulePhase.class)) {
            @SuppressWarnings("unchecked")
            Class<? extends RulePhase> unwrappedClass = (Class<? extends RulePhase>) Proxies.unwrap(phase).getClass();
            String simpleName = unwrappedClass.getSimpleName();
            phases.put(classNameToMapKey(simpleName), unwrappedClass);
        }
        return Collections.unmodifiableMap(phases);
    }

    /**
     * Returns the phases loaded in this finder, sorted by Class.getSimpleName().
     */
    public List<Class<? extends RulePhase>> getAvailablePhases() {
        ArrayList<Class<? extends RulePhase>> phases = new ArrayList<>(this.cachedPhases.values());
        // It could be sorted by the real order.
        phases.sort(new Comparator() {
            @Override
            public int compare(Object phaseClass1, Object phaseClass2) {
                if (phaseClass1 == null || !(phaseClass1 instanceof Class))
                    return -1;
                if (phaseClass2 == null || !(phaseClass2 instanceof Class))
                    return 1;

                String name1 = ((Class<? extends RulePhase>) phaseClass1).getSimpleName();
                String name2 = ((Class<? extends RulePhase>) phaseClass2).getSimpleName();
                return name1.compareToIgnoreCase(name2);
            }
        });
        return phases;
    }

}
