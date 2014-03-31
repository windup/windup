/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Initial API and implementation
*/
package org.jboss.windup.interrogator.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.archetype.VendorResult;
import org.jboss.windup.metadata.type.archive.ZipMetadata;
import org.jboss.windup.util.CustomerPackageResolver;


public class KnownArchiveProfiler {

	private static final Log LOG = LogFactory.getLog(KnownArchiveProfiler.class);
	private Map<Pattern, String> knownPackages;
	private CustomerPackageResolver customerPackageResolver;

	public void setCustomerPackageResolver(CustomerPackageResolver customerPackageResolver) {
		this.customerPackageResolver = customerPackageResolver;
	}

	public void setKnownPackages(Map<String, String> knownPackagesSet) {
		this.knownPackages = compilePatternSet(knownPackagesSet);
	}

	protected Map<Pattern, String> compilePatternSet(Map<String, String> patternStringSet) {
		if (patternStringSet == null)
			return null;

		Map<Pattern, String> target = new HashMap<Pattern, String>(patternStringSet.size());
		for (String patternString : patternStringSet.keySet()) {
			String patternVal = patternStringSet.get(patternString);

			if (!StringUtils.startsWith(patternString, "^")) {
				patternString = "^" + patternString;
			}

			target.put(Pattern.compile(patternString), patternVal);
		}

		return target;
	}

	public boolean isKnownVendor(ZipMetadata archive, String pkg) {
		for (Pattern cbl : knownPackages.keySet()) {
			if (cbl.matcher(pkg).find()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found known package: " + pkg + " matching: " + cbl.pattern());
				}
				String vendor = knownPackages.get(cbl);

				VendorResult vr = new VendorResult();
				vr.setDescription(vendor);
				archive.getDecorations().add(vr);

				return true;
			}
		}
		return false;
	}

	public boolean isExclusivelyKnownArchive(ZipMetadata archive) {
		String extension = archive.getFilePointer().getAbsolutePath();
		extension = StringUtils.substringAfterLast(extension, ".");

		if (!StringUtils.equalsIgnoreCase(extension, "jar")) {
			return false;
		}

		VendorResult vr = null;

		// this should only be true if:
		// 1) the package does not contain *any* customer packages.
		// 2) the package contains "known" vendor packages.
		boolean exclusivelyKnown = false;

		Enumeration<?> e = archive.getZipFile().entries();

		// go through all entries...
		ZipEntry entry;
		while (e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();
			String entryName = getClassNameFromFile(entry.getName());

			// if the package isn't current "known", try to match against known packages for this entry.
			if (!exclusivelyKnown) {
				for (Pattern cbl : knownPackages.keySet()) {
					if (cbl.matcher(entryName).find()) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Found known package: " + entry.getName() + " matching: " + cbl.pattern());
						}
						String vendor = knownPackages.get(cbl);
						LOG.debug("Known Package: " + archive.getName() + "; Vendor: " + vendor);
						exclusivelyKnown = true;

						vr = new VendorResult();
						vr.setDescription(vendor);
						// no reason to check other patterns.
						break;
					}
				}
			}

			// check each entry to see if it is a customer package.
			if (customerPackageResolver.isCustomerPkg(entryName)) {
				// if it is a customer package, then we want to basically say, this is not exclusively a known package.
				// we can stop all other processing.
				return false;
			}
		}
		if (exclusivelyKnown && vr != null) {
			archive.getDecorations().add(vr);
		}

		// otherwise, return the evaluated exclusively known value.
		return exclusivelyKnown;
	}

	protected String getClassNameFromFile(String entryName) {
		String className = StringUtils.replace(entryName, "\\", "/");
		className = StringUtils.removeStart(className, "/");
		className = StringUtils.replace(className, "/", ".");
		className = StringUtils.substringBefore(className, "$");

		// account for WAR classes.
		if (StringUtils.contains(className, "WEB-INF.classes.")) {
			className = StringUtils.substringAfter(className, "WEB-INF.classes.");
		}
		if (StringUtils.contains(className, "META-INF.classes.")) {
			className = StringUtils.substringAfter(className, "META-INF.classes.");
		}
		return className;
	}
}
