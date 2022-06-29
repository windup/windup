package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ArchiveMetadataExtractionPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.LicenseModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.enterprise.inject.Vetoed;

/**
 * Discovers Licenses files within archives and adds the {@link LicenseModel} type to the {@link FileModel}.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
@Vetoed
@RuleMetadata(phase = ArchiveMetadataExtractionPhase.class, perform = "DiscoverArchiveLicenseFiles")
public class DiscoverArchiveLicenseFilesRuleProvider extends IteratingRuleProvider<ArchiveModel> {
    private static final Logger LOG = Logging.get(DiscoverArchiveLicenseFilesRuleProvider.class);

    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    @Override
    public ConditionBuilder when() {
        return Query.fromType(ArchiveModel.class);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context,
                        ArchiveModel payload) {
        Rule rule = (Rule) context.get(Rule.class);
        Set<FileModel> licenseFiles = findLicense(payload);
        if (licenseFiles.isEmpty()) {
            // no licenses found, skip this one
            return;
        }

        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
        GraphService<LicenseModel> licenseService = new GraphService<>(event.getGraphContext(), LicenseModel.class);

        for (FileModel license : licenseFiles) {
            LOG.info("Classifying: " + license.getFileName()
                    + " as License within archive: " + payload.getArchiveName());

            // http://opensource.org/licenses/
            try (InputStream stream = license.asInputStream()) {
                String content = IOUtils.toString(stream);

                if (StringUtils.containsIgnoreCase(content, "Apache License, Version 2.0")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "Apache License 2.0",
                            "Apache License 2.0 File",
                            "http://www.apache.org/licenses/LICENSE-2.0");
                } else if (StringUtils.containsIgnoreCase(content, "Apache Software License, Version 1.1")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "Apache License 1.1",
                            "Apache License 1.1 File",
                            "http://www.apache.org/licenses/LICENSE-1.1");
                } else if (StringUtils.containsIgnoreCase(content, "Copyright (c) 1995-1999 The Apache Group.  All rights reserved.")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "Apache License 1.0",
                            "Apache License 1.0 File",
                            "http://www.apache.org/licenses/LICENSE-1.0");
                } else if (StringUtils.containsIgnoreCase(content, "GNU General Public License")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "GNU GPL",
                            "GNU General Public License File",
                            "http://opensource.org/licenses/gpl-license");
                } else if (StringUtils.containsIgnoreCase(content, "The MIT License (MIT)")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "MIT License",
                            "MIT License File",
                            "http://opensource.org/licenses/MIT");
                } else if (StringUtils.containsIgnoreCase(content, "Mozilla Public License, version 2.0")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "Mozilla Public License 2.0",
                            "Mozilla Public License 2.0 File",
                            "http://opensource.org/licenses/MPL-2.0");
                } else if (StringUtils.containsIgnoreCase(content, "GNU Lesser General Public License")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "GNU LGPL",
                            "GNU LGPL File",
                            "http://opensource.org/licenses/lgpl-license");
                } else if (StringUtils.contains(content, "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "CDDL",
                            "CDDL License File",
                            "http://opensource.org/licenses/CDDL-1.0");
                } else if (StringUtils.containsIgnoreCase(content, "Eclipse Public License")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "Eclipse Public License 1.0",
                            "Eclipse Public License 1.0 File",
                            "http://opensource.org/licenses/EPL-1.0");
                } else if (StringUtils.containsIgnoreCase(content, "Redistribution and use in source and binary forms")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "BSD License",
                            "BSD License File",
                            "http://opensource.org/licenses/");
                } else if (StringUtils.containsIgnoreCase(content, "the work of authorship identified is in the public domain of the country")) {
                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "Public Domain License",
                            "Creative Commons Public Domain License File",
                            "http://creativecommons.org/licenses/publicdomain/");
                } else {
                    LOG.warning("Must be unknown license type: " + license.getFileName());

                    tagLicenseByTechnologyTag(rule, licenseService,
                            technologyTagService,
                            license,
                            "Unknown License", "Unknown License File", "Unknown License File");
                }
            } catch (IOException e) {
                LOG.fine("Error while opening License file: " + license.getFileName()
                        + " with error: " + e.getLocalizedMessage());
            }
        }

    }

    private void tagLicenseByTechnologyTag(Rule rule, GraphService<LicenseModel> licenseService,
                                           TechnologyTagService technologyTagService, FileModel license,
                                           String name, String description, String url) {
        LOG.info("Identified: " + license.getFileName() + " as: " + name);

        // create license model for future reporting.
        LicenseModel model = licenseService.addTypeToModel(license);
        model.setName(name);
        model.setURL(url);
        // even it is not classified or marked as a hint, do include it in reports
        model.setGenerateSourceReport(true);

        // also add it as a tag for reporting on main screen.
        technologyTagService.addTagToFileModel(license, name, TECH_TAG_LEVEL);
    }

    private Set<FileModel> findLicense(ArchiveModel archive) {
        Set<FileModel> licenses = new HashSet<>();

        Iterable<FileModel> files = archive.getAllFiles();
        for (FileModel model : files) {
            if (model.isDirectory()) {
                continue;
            }

            String fileName = model.getFileName();
            fileName = StringUtils.lowerCase(fileName);

            if (fileName.endsWith("license.txt")
                    || fileName.endsWith("license")
                    || fileName.endsWith("gpl.txt")
                    || fileName.endsWith("lgpl.txt")
                    || fileName.endsWith("notice.txt")
                    || fileName.endsWith("notice")) {
                licenses.add(model);
            }
        }

        return licenses;
    }

}
