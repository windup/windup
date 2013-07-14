package org.jboss.windup.reporting.integration.forge;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;

public class SwitchyardReporter implements Reporter {

	@Override
	public void process(ArchiveMetadata archive, File reportDirectory) {
		File forgeOutput = new File(reportDirectory, "forge");
		try {
			FileUtils.forceMkdir(forgeOutput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
