package org.jboss.windup.reporting.data.rules;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPf4RenderingPhase;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.reporting.data.dto.ApplicationTechnologiesDto;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechReportModel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.exception.WindupException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@RuleMetadata(
        phase = ReportPf4RenderingPhase.class,
        haltOnException = true
)
public class TechnologiesRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "technologies";

    public static final String MAPPING_OF_PLACEMENT_NAMES = "techReport:mappingOfPlacementTagNames";

    private static final Logger LOG = Logger.getLogger(TechnologiesRuleProvider.class.getName());

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();

        TagGraphService tagGraphService = new TagGraphService(event.getGraphContext());
        TagModel sectorsTag = tagGraphService.getTagByName(TechReportModel.EDGE_TAG_SECTORS);

        List<ApplicationTechnologiesDto> result = new ArrayList<>();

        // Create models for each app.
        for (ProjectModel application : new ProjectService(event.getGraphContext()).getRootProjectModels()) {
            Map<String, Map<String, Map<String, Integer>>> allTechnologiesForApp = new HashMap<>();

            TechStatsMatrix techStatsMatrix = getTechStatsMap(context, application);

            sectorsTag.getDesignatedTags().forEach(tagModel -> {
                // Sector: View, Connect, Store, Sustain, Execute
                String sectorTitle = tagModel.getTitle();
                Map<String, Map<String, Integer>> sectorValue = new HashMap<>();
                allTechnologiesForApp.put(sectorTitle, sectorValue);

                tagModel.getDesignatedTags().forEach(subTagModel -> {
                    // Markup, MVC, Rich, Web || Binding, EJB, HTTP, Messaging, Other
                    String technologyTitle = subTagModel.getTitle();
                    Map<String, Integer> technologyValue = new HashMap<>();

                    if (!technologyTitle.equals(sectorTitle)) {
                        sectorValue.put(technologyTitle, technologyValue);

                        // Tags per technology
                        String boxName = subTagModel.getName();
                        Map<String, TechUsageStatSum> sum = techStatsMatrix.getSummarizedStatsByTechnology(boxName, application.getId());

                        for (Map.Entry<String, TechUsageStatSum> entry : sum.entrySet()) {
                            if (entry.getValue() != null && !entry.getValue().getName().isBlank()) {
                                technologyValue.put(entry.getValue().getName(), entry.getValue().getOccurrenceCount());
                            }
                        }
                    }
                });
            });

            ApplicationTechnologiesDto applicationTechnologiesDto = new ApplicationTechnologiesDto();
            applicationTechnologiesDto.applicationId = application.getId().toString();
            applicationTechnologiesDto.technologyGroups = allTechnologiesForApp;
            result.add(applicationTechnologiesDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

    private static Set<String> getPlacementTags(GraphContext graphContext, Set<String> potentialPlaceTags) {
        final TagGraphService tagService = new TagGraphService(graphContext);

        Set<String> placeNames = new HashSet<>();
        potentialPlaceTags.forEach(name -> {
            final TagModel placeTag = tagService.getTagByName("place:" + Tag.normalizeName(name));
            if (null != placeTag) {
                placeNames.add(placeTag.getName());
            }
        });
        return placeNames;
    }

    private static TechReportPlacement processPlaceLabels(GraphContext graphContext, Set<String> tagNames) {
        TagGraphService tagService = new TagGraphService(graphContext);

        if (tagNames.size() < 3) {
            throw new WindupException("There should always be exactly 3 placement labels - row, sector, column/box. It was: " + tagNames);
        }
        if (tagNames.size() > 3) {
            LOG.severe("There should always be exactly 3 placement labels - row, sector, column/box. It was: " + tagNames);
        }

        TechReportPlacement placement = new TechReportPlacement();

        final TagModel placeSectorsTag = tagService.getTagByName("techReport:placeSectors");
        final TagModel placeBoxesTag = tagService.getTagByName("techReport:placeBoxes");
        final TagModel placeRowsTag = tagService.getTagByName("techReport:placeRows");

        Set<String> unknownTags = new HashSet<>();
        for (String name : tagNames) {
            final TagModel tag = tagService.getTagByName(name);
            if (null == tag)
                continue;

            if (TagGraphService.isTagUnderTagOrSame(tag, placeSectorsTag)) {
                placement.sector = tag;
            } else if (TagGraphService.isTagUnderTagOrSame(tag, placeBoxesTag)) {
                placement.box = tag;
            } else if (TagGraphService.isTagUnderTagOrSame(tag, placeRowsTag)) {
                placement.row = tag;
            } else {
                unknownTags.add(name);
            }
        }
        placement.unknown = unknownTags;

        LOG.fine(String.format("\t\tLabels %s identified as: sector: %s, box: %s, row: %s", tagNames, placement.sector, placement.box, placement.row));
        if (placement.box == null || placement.row == null) {
            LOG.severe(String.format("There should always be exactly 3 placement labels - row, sector, column/box. Found: %s, of which box: %s, row: %s", tagNames, placement.box, placement.row));
        }
        return placement;
    }

    private static TechReportPlacement normalizePlacement(GraphContext graphContext, TechReportPlacement placement) {
        TagGraphService tagService = new TagGraphService(graphContext);

        final TechReportPlacement normalPlacement = new TechReportPlacement();
        normalPlacement.sector = getNonPlaceParent(tagService, placement.sector);
        normalPlacement.box = getNonPlaceParent(tagService, placement.box);
        normalPlacement.row = getNonPlaceParent(tagService, placement.row);
        return normalPlacement;
    }

    private static TagModel getNonPlaceParent(TagGraphService tagService, TagModel tag) {
        if (tag == null) {
            return null;
        }

        final TagModel placeRoot = tagService.getTagByName(MAPPING_OF_PLACEMENT_NAMES);

        final Iterator<TagModel> parents = tag.getDesignatedByTags().iterator();
        if (!parents.hasNext()) {
            throw new WindupException("Tag is not designated by any tags: " + tag);
        }

        TagModel nonPlaceParent = null;
        while (parents.hasNext()) {
            TagModel parentTag = parents.next();
            if (TagGraphService.isTagUnderTagOrSame(parentTag, placeRoot)) {
                continue;
            }
            if (nonPlaceParent != null) {
                throw new WindupException(String.format("Tag %s has more than one non-placement parent: %s, %s", tag.getName(), nonPlaceParent, parentTag));
            }
            nonPlaceParent = parentTag;
        }

        return nonPlaceParent;
    }

    private static void mergeToTheRightCell(
            Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> matrix,
            String rowName,
            String boxName,
            Long projectKey,
            String techLabel,
            TechnologyUsageStatisticsModel stat, boolean maxInsteadOfAdd) {
        final Map<String, Map<Long, Map<String, TechUsageStatSum>>> rowAll = matrix.computeIfAbsent(rowName, k -> new HashMap());
        final Map<Long, Map<String, TechUsageStatSum>> boxAll = rowAll.computeIfAbsent(boxName, k -> new HashMap<>());
        final Map<String, TechUsageStatSum> statSum = boxAll.computeIfAbsent(projectKey, k -> new HashMap<>());
        final TechUsageStatSum techUsageStatSum = statSum.computeIfAbsent(techLabel, k -> new TechUsageStatSum(techLabel));
        if (maxInsteadOfAdd) {
            techUsageStatSum.max(stat);
        } else {
            techUsageStatSum.add(stat);
        }
    }


    public TechStatsMatrix getTechStatsMap(GraphContext context, ProjectModel application) {
        Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map = new HashMap<>();

        final Iterable<TechnologyUsageStatisticsModel> statModels = context.service(TechnologyUsageStatisticsModel.class).findAll();
        for (TechnologyUsageStatisticsModel stat : statModels) {
            //If a stat doesn't apply to this project we move on to the next stat without further processing
            if (application != null) {
                boolean appIsContainedInStatProject = stat.getProjectModel().getApplications().contains(application);
                boolean appIsASharedLibrary = application.getName().toLowerCase().contains("shared");
                boolean statAppliesToSeveralApps = stat.getProjectModel().getApplications().size() > 1;
                if (!appIsContainedInStatProject || (statAppliesToSeveralApps && !appIsASharedLibrary)) {
                    LOG.fine("\t\tThis stat is not for this project, skipping.");
                    continue;
                }
            }

            final Set<String> placementTags = getPlacementTags(context, stat.getTags());
            TechReportPlacement placement = processPlaceLabels(context, placementTags);
            if (placement.box == null || placement.row == null) {
                LOG.severe(String.format("\tPlacement labels not recognized, placement incomplete: %s; stat: %s", placement, stat));
                continue;
            }

            placement = normalizePlacement(context, placement);

            if (placement.box == null || placement.row == null) {
                LOG.severe(String.format("\tPlacement labels not recognized, placement incomplete: %s; stat: %s", placement, stat));
                continue;
            }

            // For boxes report - show each tech in sector, row, box. A sum for all projects.
            mergeToTheRightCell(map, placement.row.getName(), placement.box.getName(), 0L, stat.getName(), stat, false);
            // For the punch card report - maximum count for each box.
            mergeToTheRightCell(map, "", placement.box.getName(), 0L, "", stat, true);

            List<Long> appsToCountTowards;
            if (application == null) {
                appsToCountTowards = StreamSupport.stream(stat.getProjectModel().getApplications().spliterator(), false)
                        .map(ProjectModel::getElement)
                        .map(Vertex::id)
                        .map(Long.class::cast)
                        .collect(toList());
            } else {
                appsToCountTowards = Collections.singletonList(application.getId());
            }

            boolean isSharedApp = appsToCountTowards.size() > 1;
            List<ProjectModel> apps = stat.getProjectModel().getApplications();
            List<ProjectModel> sharedApps = apps.stream().filter(app -> app.getName().toLowerCase().contains("shared")).collect(toList());


            for (Long appToCountTowards : appsToCountTowards) {
                if (isSharedApp) {
                    if (!sharedApps.stream().anyMatch(app -> app.getElement().id().equals(appToCountTowards))) {
                        continue;
                    }
                }

                // For boxes report - show each tech in sector, row, box. For individual projects.
                mergeToTheRightCell(map, placement.row.getName(), placement.box.getName(), appToCountTowards, stat.getName(), stat, false);

                // For the punch card report - roll up rows and individual techs.
                mergeToTheRightCell(map, "", placement.box.getName(), appToCountTowards, "", stat, false);
            }
        }

        return new TechStatsMatrix(map);
    }

    public static class TechReportPlacement {
        TagModel sector;
        TagModel box;
        TagModel row;
        Set<String> unknown;

        @Override
        public String toString() {
            return "TechReportPlacement{sector=" + sector + ", box=" + box + ", row=" + row + ", unknown=" + unknown + '}';
        }
    }

    public static class TechUsageStatSum {
        String name;
        int count = 0;
        Set<String> tags = new HashSet<>();

        public TechUsageStatSum(String name) {
            this.name = name;
        }

        public TechUsageStatSum max(TechnologyUsageStatisticsModel stat) {
            this.count = Math.max(count, stat.getOccurrenceCount());
            return this;
        }

        public TechUsageStatSum add(TechnologyUsageStatisticsModel stat) {
            if (!"".equals(this.name) && !this.name.equals(stat.getName())) {
                throw new IllegalArgumentException("Can't add up stats, " + this.name + " != " + stat.getName());
            }
            this.count += stat.getOccurrenceCount();
            this.tags.addAll(stat.getTags());
            return this;
        }

        public TechUsageStatSum add(TechUsageStatSum stat) {
            if (!"".equals(this.name) && !this.name.equals(stat.getName())) {
                throw new IllegalArgumentException("Can't add up stats, " + this.name + " != " + stat.getName());
            }
            this.count += stat.getOccurrenceCount();
            this.tags.addAll(stat.getTags());
            return this;
        }

        public String getName() {
            return name;
        }

        public int getOccurrenceCount() {
            return count;
        }

        public Set<String> getTags() {
            return tags;
        }

        @Override
        public String toString() {
            return "{" + name + " " + count + "×, [" + tags + "]}";
        }
    }

    public static class TechStatsMatrix {
        // Prepares a precomputed matrix -
        // map of maps of maps: rowTag -> boxTag -> project -> placement label -> TechUsageStatSum.
        private Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map = new HashMap<>();

        public TechStatsMatrix(Map<String, Map<String, Map<Long, Map<String, TechUsageStatSum>>>> map) {
            this.map = map;
        }

        public Map<String, TechUsageStatSum> getSummarizedStatsByTechnology(String boxTagName, Long projectId) {
            Set<String> rowTagNames = map.keySet();
            Map<String, TechUsageStatSum> interimStatMap = new LinkedHashMap<String, TechUsageStatSum>();
            Map<String, TechUsageStatSum> returnStatMap = new LinkedHashMap<String, TechUsageStatSum>();
            rowTagNames.forEach(rowTagName -> {
                final Map<String, Map<Long, Map<String, TechUsageStatSum>>> rowMap = map.get(rowTagName);
                final Map<Long, Map<String, TechUsageStatSum>> boxMap = rowMap.get(boxTagName);
                if (boxMap != null) {
                    Map<String, TechUsageStatSum> statMap = boxMap.get(projectId);
                    Set<String> interimStatKeys = interimStatMap.keySet();
                    if (statMap != null) {
                        statMap.keySet().forEach(statKey -> {
                            if (interimStatKeys.contains(statKey)) {
                                interimStatMap.get(statKey).count += statMap.get(statKey).count;
                            } else {
                                interimStatMap.put(statKey, statMap.get(statKey));
                            }
                        });
                    }
                }
            });

            //have the blank key item (title row) first
            returnStatMap.put("", interimStatMap.get(""));
            //sort interim map and populate return map, leaving out blank key item (which has been placed in already as the first item)
            interimStatMap.entrySet().stream()
                    .filter(e -> e.getKey() != "")
                    .sorted(Map.Entry.<String, TechUsageStatSum>comparingByValue(new TechUsageStatSumComparator()).reversed())
                    .forEachOrdered(x ->
                            returnStatMap.put((String) ((Map.Entry) x).getKey(), (TechUsageStatSum) ((Map.Entry) x).getValue())
                    );

            return returnStatMap;
        }
    }

    public static class TechUsageStatSumComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if (!(o1 instanceof TechUsageStatSum) || !(o2 instanceof TechUsageStatSum)) {
                throw new ClassCastException();
            }
            if (((TechUsageStatSum) o1).getOccurrenceCount() < ((TechUsageStatSum) o2).getOccurrenceCount()) {
                return -1;
            } else if (((TechUsageStatSum) o1).getOccurrenceCount() > ((TechUsageStatSum) o2).getOccurrenceCount()) {
                return 1;
            } else {
                return 0;
            }

        }
    }
}
