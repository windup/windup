package org.jboss.windup.classprofiler.lucene.transformer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.jboss.windup.classprofiler.metadata.ClassVO;

public class ClassTransformer {
	public static final String QUALIFIED_NAME = "qualifiedClassName";
	public static final String DEPENDENCY = "classDependency";
	
	private ClassTransformer() {
		//seal
	}
	
	public static ClassVO fromDocument(Document doc) {
		ClassVO classVO = new ClassVO();
		classVO.setQualifiedName(doc.get(QUALIFIED_NAME));

		for(IndexableField dep : doc.getFields(DEPENDENCY)) {
			classVO.getDependencies().add(dep.stringValue());
		}
		
		return classVO;
	}

	public static Document toDocument(ClassVO clz) {
		Document document = new Document();
		document.add(new StringField(QUALIFIED_NAME, clz.getQualifiedName(), Field.Store.YES));
		
		if(clz.getDependencies() != null) {
			for(String dep : clz.getDependencies()) {
				document.add(new StringField(DEPENDENCY, dep, Field.Store.YES));
			}
		}
		
		return document;
	}


}
