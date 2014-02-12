package org.jboss.windup.graph.model.meta.javaclass;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("HibernateEntityFacet")
public interface HibernateEntityFacet extends JavaClassMetaFacet {

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
