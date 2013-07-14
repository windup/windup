package org.jboss.windup.reporting.integration.forge;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;

public class SwitchyardReporter implements Reporter {
	private static final Log LOG = LogFactory.getLog(SwitchyardReporter.class);
	private static final String ESB_DESC = "JBoss ESB Pipeline Configuration";
	
	@Override
	public void process(ArchiveMetadata archive, File reportDirectory) {
		if (isJBossESB(archive)) {
			LOG.info("Generating Switchyard compontents leveraging JBoss Forge.");
			SwitchyardForgeController sfc = new SwitchyardForgeController();
			sfc.process(archive, reportDirectory);
		}
	}

	/***
	 * Recursively checks for the JBoss ESB configuration.    
	 * @param archive
	 * @return True if JBoss ESB Configuration is present.
	 */
	protected boolean isJBossESB(ArchiveMetadata archive) {
		for (FileMetadata entry : archive.getEntries()) {
			//only look for decorations if it is an XML file.
			if(entry instanceof XmlMetadata) {
				for (AbstractDecoration dec : entry.getDecorations()) {
					if (dec instanceof Classification) {
						if (StringUtils.equals(dec.getDescription(), ESB_DESC)) {
							return true;
						}
					}
				}
			}
		}
		

		//recurse.
		if(archive.getNestedArchives() != null) {
			for(ArchiveMetadata ar : archive.getNestedArchives()) {
				if(isJBossESB(ar)) {
					return true;
				}
			}
		}
		
		return false;
	}

}
