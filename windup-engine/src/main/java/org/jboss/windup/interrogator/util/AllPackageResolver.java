package org.jboss.windup.interrogator.util;

import org.jboss.windup.util.CustomerPackageResolver;

public class AllPackageResolver extends CustomerPackageResolver {

	public AllPackageResolver(String packageSignatures, String excludeSignatures) {
		super(packageSignatures, excludeSignatures);
	}

	@Override
	public boolean isCustomerPkg(String className) {
		return true;
	}
}
