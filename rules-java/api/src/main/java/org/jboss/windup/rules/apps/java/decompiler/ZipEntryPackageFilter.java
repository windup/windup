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
		name = StringUtils.removeStart(name, "/WEB-INF/classes");
		
		for(String filter : filters) {
			
			if(StringUtils.startsWith(what.getName(), filter)) {
				log.info("Accepting: "+what.getName());
				return Result.ACCEPT;
			}
		}
		log.info("Rejecting: "+what.getName()+" -- Name: "+name);
		return Result.REJECT;
	}

}
