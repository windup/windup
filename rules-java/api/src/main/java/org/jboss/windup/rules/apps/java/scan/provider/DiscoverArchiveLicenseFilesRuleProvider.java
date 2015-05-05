package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
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
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.ClassificationService;
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
		IteratingRuleProvider<ArchiveModel> {
	private static final Logger LOG = Logger
			.getLogger(DiscoverArchiveLicenseFilesRuleProvider.class
					.getSimpleName());

	private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

	public DiscoverArchiveLicenseFilesRuleProvider() {
		super(MetadataBuilder.forProvider(
				DiscoverArchiveLicenseFilesRuleProvider.class).setPhase(
				ArchiveMetadataExtractionPhase.class));
	}

	@Override
	public String toStringPerform() {
		return "DiscoverArchiveLicenseFiles";
	}

	@Override
	public ConditionBuilder when() {
		return Query.fromType(ArchiveModel.class);
	}

	@Override
	public void perform(GraphRewrite event, EvaluationContext context,
			ArchiveModel payload) {
		List<FileModel> licenseFiles = findLicense(payload);
		if (licenseFiles.size() == 0) {
			// no licenses found, skip this one
			return;
		}

		TechnologyTagService technologyTagService = new TechnologyTagService(
				event.getGraphContext());
		ClassificationService classificationService = new ClassificationService(
				event.getGraphContext());
		GraphService<LicenseModel> licenseService = new GraphService<LicenseModel>(
				event.getGraphContext(), LicenseModel.class);

		for (FileModel license : licenseFiles) {
			LOG.info("Classifying: " + license.getFileName()
					+ " as License within archive: " + payload.getArchiveName());
			classificationService.attachClassification(license, "License",
					"License File.");

			// http://opensource.org/licenses/
			try (InputStream stream = license.asInputStream()) {
				String content = IOUtils.toString(stream);

				if (StringUtils.containsIgnoreCase(content,
						"Apache License, Version 2.0")) {
					tagLicense(licenseService, technologyTagService, license,
							"Apache License 2.0",
							"http://www.apache.org/licenses/LICENSE-2.0",
							"Apache License 2.0");
				} else if (StringUtils.containsIgnoreCase(content,
						"GNU General Public License")) {
					tagLicense(licenseService, technologyTagService, license,
							"GNU General Public License",
							"http://opensource.org/licenses/gpl-license",
							"GNU License");
				} else if (StringUtils.containsIgnoreCase(content,
						"The MIT License (MIT)")) {
					tagLicense(licenseService, technologyTagService, license,
							"MIT License",
							"http://opensource.org/licenses/MIT", "MIT License");
				} else if (StringUtils.containsIgnoreCase(content,
						"Mozilla Public License, version 2.0")) {
					tagLicense(licenseService, technologyTagService, license,
							"Mozilla Public License 2.0",
							"http://opensource.org/licenses/MPL-2.0",
							"Mozilla Public License 2.0");
				} else if (StringUtils.containsIgnoreCase(content,
						"GNU Lesser General Public License")) {
					tagLicense(licenseService, technologyTagService, license,
							"GNU LGPL",
							"http://opensource.org/licenses/lgpl-license",
							"GNU LGPL");
				} else if (StringUtils.containsIgnoreCase(content,
						"COMMON DEVELOPMENT AND DISTRIBUTION LICENSE")) {
					tagLicense(licenseService, technologyTagService, license,
							"CDDL", "http://opensource.org/licenses/CDDL-1.0",
							"CDDL License");
				} else if (StringUtils.containsIgnoreCase(content,
						"Eclipse Public License")) {
					tagLicense(licenseService, technologyTagService, license,
							"Eclipse Public License",
							"http://opensource.org/licenses/EPL-1.0",
							"Eclipse Public License");
				} else {
					LOG.warning("Must be unknown license type: "
							+ license.getFileName());
					tagLicense(licenseService, technologyTagService, license,
							"Unknown License", "", "Unknown License");
				}
			} catch (IOException e) {
				// should do something here...
			}
		}

	}

	private void tagLicense(GraphService<LicenseModel> licenseService,
			TechnologyTagService technologyTagService, FileModel license,
			String name, String url, String tagName) {
		LOG.info("Identified: " + license.getFileName() + " as: " + name);

		LicenseModel model = licenseService.addTypeToModel(license);
		model.setName(name);
		model.setURL(url);

		// also add it as a tag.
		technologyTagService
				.addTagToFileModel(license, tagName, TECH_TAG_LEVEL);
	}

	private List<FileModel> findLicense(ArchiveModel archive) {
		List<FileModel> licenses = new LinkedList<>();

		Iterable<FileModel> files = archive.getContainedFileModels();
		for (FileModel model : files) {
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
