package org.jboss.windup.rules.apps.java.decompiler;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.decompiler.util.Filter;
import org.jboss.windup.rules.apps.java.model.PackageModel;

public class ZipEntryPackageFilter implements Filter<ZipEntry> {
	private static final Logger log = Logger.getLogger(ZipEntryPackageFilter.class.getName());
	
	Set<String> filters = new HashSet<>();
	public ZipEntryPackageFilter(Iterable<PackageModel> packages) {
		if(packages != null) {
			for(PackageModel model : packages) {
				String packageName = model.getPackageName();
				String entryFilter = StringUtils.replace(packageName, ".", "/");
				log.info("Initializing with filter: "+entryFilter);
				filters.add(entryFilter);
			}
		}
	}
	
	@Override
	public org.jboss.windup.decompiler.util.Filter.Result decide(ZipEntry what) {
		
		//if there aren't any packages defined, return true to accept all classes.
		if(filters.size() == 0) {
			return Result.ACCEPT;
		}
		
		String name = what.getName();
		
		if(StringUtils.startsWith(name, "WEB-INF/classes/")) {
			//WAR file
			name = StringUtils.removeStart(name, "WEB-INF/classes/");
		}
		else if(StringUtils.startsWith(name, "classes/")) {
			//PAR file (jBPM Process Archive)
			name = StringUtils.removeStart(name, "classes/");
		}
		else if(StringUtils.startsWith(name, "service/")) {
			//SAR file (JBoss Service Archive)
			name = StringUtils.removeStart(name, "service/");
		}
		
		for(String filter : filters) {
			
			if(StringUtils.startsWith(what.getName(), filter)) {
				log.fine("Accepting: "+what.getName()+" -- Name: "+name);
				return Result.ACCEPT;
			}
		}
		log.fine("Rejecting: "+what.getName()+" -- Name: "+name);
		return Result.REJECT;
	}

}
