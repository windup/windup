package com.acme.anvil.service;

import javax.ejb.EJBObject;

public interface ProductCatalog extends EJBObject{
    void populateCatalog();
}
