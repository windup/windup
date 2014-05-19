package com.acme.anvil.service;

import javax.ejb.EJBLocalObject;

public interface ProductCatalogLocal extends EJBLocalObject {
	public void populateCatalog();
}
