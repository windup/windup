package org.jboss.windup.reporting.integration.forge;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;

public class SwitchyardReporter implements Reporter {
	private static final Log LOG = LogFactory.getLog(SwitchyardReporter.class);

	@Override
	public void process(ArchiveMetadata archive, File reportDirectory) {
		if (isJBossESB(archive)) {
			LOG.info("Generating Switchyard compontents leveraging JBoss Forge.");
			SwitchyardForgeController sfc = new SwitchyardForgeController();
			sfc.process(archive, reportDirectory);
		}
	}

	public boolean isJBossESB(ArchiveMetadata archive) {
		for (FileMetadata entry : archive.getEntries()) {
			for (AbstractDecoration dec : entry.getDecorations()) {
				if (dec instanceof Classification) {
					if (StringUtils.equals(dec.getDescription(),
							"JBoss ESB Pipeline Configuration")) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
