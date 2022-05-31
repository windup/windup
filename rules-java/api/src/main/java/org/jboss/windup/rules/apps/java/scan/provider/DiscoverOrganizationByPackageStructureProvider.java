package org.jboss.windup.rules.apps.java.scan.provider;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ClassifyFileTypesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.service.OrganizationService;
import org.jboss.windup.rules.apps.java.scan.operation.packagemapping.PackageNameMapping;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Look at the package structure of the archive. If the packages all start with known organization's package structure, then mark as potential organization.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = ClassifyFileTypesPhase.class)
public class DiscoverOrganizationByPackageStructureProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(DiscoverOrganizationByPackageStructureProvider.class);

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(ArchiveModel.class))
                .perform(
                        new AbstractIterationOperation<ArchiveModel>() {
                            public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload) {
                                final OrganizationService organizationService = new OrganizationService(event.getGraphContext());
                                LOG.info("Processing Archive: " + payload.getArchiveName());
                                Set<String> packageSet = new HashSet<>();
                                Set<String> possibleOrganization = new HashSet<>();

                                File archiveFile = payload.asFile();
                                try (ZipFile zipFile = new ZipFile(archiveFile)) {
                                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                                    while (entries.hasMoreElements()) {
                                        ZipEntry entry = entries.nextElement();

                                        if (!entry.isDirectory()) {
                                            if (StringUtils.endsWith(entry.getName(), ".class")) {
                                                String pkg = findPackage(payload, entry.getName());
                                                packageSet.add(pkg);
                                            }
                                        }
                                    }

                                    for (String pkg : packageSet) {
                                        String organization = findOrganization(event, pkg);
                                        if (organization != null) {
                                            possibleOrganization.add(organization);
                                        }
                                    }
                                } catch (IOException e) {
                                    LOG.warning("Error loading archive: " + payload.getFileName());
                                }

                                if (possibleOrganization.isEmpty()) {
                                    LOG.info(" -- Archive: " + payload.getArchiveName() + " organization unknown.");
                                    organizationService.attachOrganization(payload, "Unknown");
                                } else if (possibleOrganization.size() > 1) {
                                    LOG.warning(" -- Archive: " + payload.getArchiveName() + " has more than one organization: ");
                                    for (String organization : possibleOrganization) {
                                        //attach the organization to the archive.
                                        organizationService.attachOrganization(payload, organization);
                                        LOG.warning("   -- " + organization);
                                    }
                                } else {
                                    organizationService.attachOrganization(payload, possibleOrganization.iterator().next());
                                    LOG.info(" -- Archive: " + payload.getFileName() + " has organization: " + possibleOrganization.iterator().next());
                                }

                            }
                        }
                );
    }
    // @formatter:on

    private String findOrganization(GraphRewrite context, String pkg) {
        return PackageNameMapping.getOrganizationFromMappings(context, pkg);
    }

    private String findPackage(final ArchiveModel payload, String entryName) {
        String packageName = StringUtils.removeEnd(entryName, ".class");
        packageName = StringUtils.replace(packageName, "/", ".");
        packageName = StringUtils.substringBeforeLast(packageName, ".");

        if (StringUtils.endsWith(payload.getArchiveName(), ".war")) {
            packageName = StringUtils.substringAfterLast(packageName, "WEB-INF.classes.");
        } else if (StringUtils.endsWith(payload.getArchiveName(), ".par")) {
            packageName = StringUtils.removeStart(packageName, "classes.");
        }
        return packageName;
    }

}
