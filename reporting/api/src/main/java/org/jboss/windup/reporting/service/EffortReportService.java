package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.EffortReportModel;

/**
 * Contains constants representing the migration effort levels.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class EffortReportService extends GraphService
{
    public static enum EffortLevel {
        INFO(0, "Info", "Info"),
        TRIVIAL(1, "Trivial", "Trivial change or 1-1 library swap"),
        COMPLEX(3, "Complex", "Complex change with documented solution"),
        REDESIGN(5, "Redesign", "Requires re-design or library change"),
        ARCHITECTURAL(7, "Requires Architectural Change", "Requires architectural decision or change"),
        UNKNOWN(13, "Unknown", "Unknown effort");

        private final int points;
        private final String shortDesc;
        private final String verboseDesc;

        private EffortLevel(final int points, final String shortDesc, final String verboseDesc)
        {
            this.points = points;
            this.shortDesc = shortDesc;
            this.verboseDesc = verboseDesc;
        }

        public static EffortLevel forPoints(int points){
            switch (points)
            {
                case 0: return INFO;
                case 1: case 2: return TRIVIAL;
                case 3: case 4: return COMPLEX;
                case 5: case 6: return REDESIGN;
                case 7: case 8: case 9: case 10: case 11: case 12: return ARCHITECTURAL;
                case 13: default: return UNKNOWN;
            }
        }


        public int getPoints()
        {
            return points;
        }


        public String getShortDescription()
        {
            return shortDesc;
        }


        public String getVerboseDescription()
        {
            return verboseDesc;
        }


    }

    public static enum Verbosity { ID, SHORT, VERBOSE };

    public EffortReportService(GraphContext context)
    {
        super(context, EffortReportModel.class);
    }

    /**
     * Returns the right string representation of the effort level based on given number of points.
     */
    public static String getEffortLevelDescription(Verbosity verbosity, int points)
    {
        EffortLevel level = EffortLevel.forPoints(points);

        switch (verbosity)
        {
            case ID:
                return level.name();
            case VERBOSE:
                return level.getVerboseDescription();
            case SHORT: default:
                return level.getShortDescription();
        }
    }

}
