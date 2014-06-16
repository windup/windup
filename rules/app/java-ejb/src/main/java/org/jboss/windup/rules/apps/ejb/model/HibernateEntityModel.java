package org.jboss.windup.rules.apps.ejb.model;

import org.jboss.windup.rules.apps.java.scan.model.JavaClassMetaModel;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("HibernateEntityFacet")
public interface HibernateEntityModel extends JavaClassMetaModel {

	@Property("specificationVersion")
	public String getSpecificationVersion();

	@Property("specificationVersion")
	public void setSpecificationVersion(String version);
	
	@Property("tableName")
	public String getTableName();
	
	@Property("tableName")
	public void setTableName(String tableName);
	
	@Property("schemaName")
	public String getSchemaName();
	
	@Property("schemaName")
	public void setSchemaName(String schemaName);
	
	@Property("catalogName")
	public String getCatalogName();
	
	@Property("catalogName")
	public void setCatalogName(String catalogName);
	
}
