package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.ArchiveMetadataExtractionPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.LicenseModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.LinkModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.LinkService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Discovers Licenses files within archives.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 * 
 */
public class DiscoverArchiveLicenseFilesRuleProvider extends
            IteratingRuleProvider<ArchiveModel>
{
    private static final Logger LOG = Logger
                .getLogger(DiscoverArchiveLicenseFilesRuleProvider.class
                            .getSimpleName());

    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    public DiscoverArchiveLicenseFilesRuleProvider()
    {
        super(MetadataBuilder.forProvider(
                    DiscoverArchiveLicenseFilesRuleProvider.class).setPhase(
                    ArchiveMetadataExtractionPhase.class));
    }

    @Override
    public String toStringPerform()
    {
        return "DiscoverArchiveLicenseFiles";
    }

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(ArchiveModel.class);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context,
                ArchiveModel payload)
    {
        Set<FileModel> licenseFiles = findLicense(payload);
        if (licenseFiles.size() == 0)
        {
            // no licenses found, skip this one
            return;
        }

        LinkService linkService = new LinkService(event.getGraphContext());
        TechnologyTagService technologyTagService = new TechnologyTagService(
                    event.getGraphContext());
        ClassificationService classificationService = new ClassificationService(
                    event.getGraphContext());
        GraphService<LicenseModel> licenseService = new GraphService<LicenseModel>(
                    event.getGraphContext(), LicenseModel.class);

        for (FileModel license : licenseFiles)
        {
            LOG.info("Classifying: " + license.getFileName()
                        + " as License within archive: " + payload.getArchiveName());

            // http://opensource.org/licenses/
            try (InputStream stream = license.asInputStream())
            {
                String content = IOUtils.toString(stream);

                if (StringUtils.containsIgnoreCase(content,
                            "Apache License, Version 2.0"))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "Apache License 2.0",
                                "Apache License 2.0 File",
                                "http://www.apache.org/licenses/LICENSE-2.0");

                    addLink(classificationService, linkService, classificationModel, "http://en.wikipedia.org/wiki/Apache_License",
                                "Apache License Wikipedia");
                    addLink(classificationService, linkService, classificationModel, "http://www.apache.org/licenses/", "Apache License Website");
                }
                else if (StringUtils.containsIgnoreCase(content,
                            "Apache Software License, Version 1.1"))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "Apache License 1.1",
                                "Apache License 1.1 File",
                                "http://www.apache.org/licenses/LICENSE-1.1");

                    addLink(classificationService, linkService, classificationModel, "http://en.wikipedia.org/wiki/Apache_License",
                                "Apache License Wikipedia");
                    addLink(classificationService, linkService, classificationModel, "http://www.apache.org/licenses/", "Apache License Website");
                }
                else if (StringUtils.containsIgnoreCase(content,
                            "Copyright (c) 1995-1999 The Apache Group.  All rights reserved."))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "Apache License 1.0",
                                "Apache License 1.0 File",
                                "http://www.apache.org/licenses/LICENSE-1.0");

                    addLink(classificationService, linkService, classificationModel, "http://en.wikipedia.org/wiki/Apache_License",
                                "Apache License Wikipedia");
                    addLink(classificationService, linkService, classificationModel, "http://www.apache.org/licenses/", "Apache License Website");
                }
                else if (StringUtils.containsIgnoreCase(content,
                            "GNU General Public License"))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "GNU GPL",
                                "GNU General Public License File",
                                "http://opensource.org/licenses/gpl-license");

                    addLink(classificationService, linkService, classificationModel, "http://en.wikipedia.org/wiki/GNU_General_Public_License",
                                "GNU License Wikipedia");

                }
                else if (StringUtils.containsIgnoreCase(content,
                            "The MIT License (MIT)"))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "MIT License",
                                "GNU General Public License File",
                                "http://opensource.org/licenses/MIT");

                    addLink(classificationService, linkService, classificationModel, "http://en.wikipedia.org/wiki/MIT_License",
                                "MIT License Wikipedia");

                }
                else if (StringUtils.containsIgnoreCase(content,
                            "Mozilla Public License, version 2.0"))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "Mozilla Public License 2.0",
                                "Mozilla Public License 2.0 File",
                                "http://opensource.org/licenses/MPL-2.0");
                    addLink(classificationService, linkService, classificationModel, "http://en.wikipedia.org/wiki/Mozilla_Public_License",
                                "Mozilla License Wikipedia");
                }
                else if (StringUtils.containsIgnoreCase(content,
                            "GNU Lesser General Public License"))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "GNU LGPL",
                                "GNU LGPL File",
                                "http://opensource.org/licenses/lgpl-license");

                    addLink(classificationService, linkService, classificationModel,
                                "http://en.wikipedia.org/wiki/GNU_Lesser_General_Public_License",
                                "GNU LGPL License Wikipedia");

                }
                else if (StringUtils.contains(content,
                            "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE"))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "CDDL",
                                "CDDL License File",
                                "http://opensource.org/licenses/CDDL-1.0");

                    addLink(classificationService, linkService, classificationModel,
                                "http://en.wikipedia.org/wiki/Common_Development_and_Distribution_License",
                                "CDDL Wikipedia");

                }
                else if (StringUtils.containsIgnoreCase(content,
                            "Eclipse Public License"))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "Eclipse Public License 1.0",
                                "Eclipse Public License 1.0 File",
                                "http://opensource.org/licenses/EPL-1.0");

                    addLink(classificationService, linkService, classificationModel, "http://en.wikipedia.org/wiki/Eclipse_Public_License",
                                "Eclipse Public License Wikipedia");
                }
                else if (StringUtils.containsIgnoreCase(content,
                            "Redistribution and use in source and binary forms"))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "BSD License",
                                "BSD License File",
                                "http://opensource.org/licenses/");

                    addLink(classificationService, linkService, classificationModel, "http://en.wikipedia.org/wiki/BSD_licenses",
                                "BSD Licenses Wikipedia");
                }
                else if (StringUtils.containsIgnoreCase(content,
                            "the work of authorship identified is in the public domain of the country"))
                {
                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "Public Domain License",
                                "Creative Commons Public Domain License File",
                                "http://creativecommons.org/licenses/publicdomain/");

                    addLink(classificationService, linkService, classificationModel, "http://en.wikipedia.org/wiki/BSD_licenses",
                                "BSD Licenses Wikipedia");
                }

                else
                {
                    LOG.warning("Must be unknown license type: "
                                + license.getFileName());

                    ClassificationModel classificationModel = tagLicense(classificationService, linkService, licenseService, technologyTagService,
                                license,
                                "Unknown License", "Unknown License File", "Unknown License File");
                }
            }
            catch (IOException e)
            {
                // should do something here...
            }
        }

    }

    private ClassificationModel tagLicense(ClassificationService classificationService, LinkService linkService,
                GraphService<LicenseModel> licenseService,
                TechnologyTagService technologyTagService, FileModel license,
                String name, String description, String url)
    {
        ClassificationModel classification = classificationService.attachClassification(license, name, description);
        LOG.info("Identified: " + license.getFileName() + " as: " + name);

        // create license model for future reporting.
        LicenseModel model = licenseService.addTypeToModel(license);
        model.setName(name);
        model.setURL(url);

        // also add it as a tag for reporting on main screen.
        technologyTagService.addTagToFileModel(license, name, TECH_TAG_LEVEL);

        return classification;
    }

    private void addLink(ClassificationService classificationService, LinkService linkService, ClassificationModel classificationModel, String url,
                String description)
    {
        LinkModel link = linkService.getOrCreate(description, url);
        classificationService.attachLink(classificationModel, link);
    }

    private Set<FileModel> findLicense(ArchiveModel archive)
    {
        Set<FileModel> licenses = new HashSet<>();

        Iterable<FileModel> files = archive.getContainedFileModels();
        for (FileModel model : files)
        {
            if (model.isDirectory())
            {
                continue;
            }

            String fileName = model.getFileName();
            fileName = StringUtils.lowerCase(fileName);

            if (fileName.endsWith("license.txt")
                        || fileName.endsWith("license")
                        || fileName.endsWith("gpl.txt")
                        || fileName.endsWith("lgpl.txt")
                        || fileName.endsWith("notice.txt")
                        || fileName.endsWith("notice"))
            {
                licenses.add(model);
            }
        }

        return licenses;
    }

}
