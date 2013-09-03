package org.jboss.windup.classprofiler.lucene.transformer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.jboss.windup.classprofiler.metadata.ArchiveVO;
import org.jboss.windup.classprofiler.metadata.ModuleVO;
import org.jboss.windup.classprofiler.metadata.PlatformVO;

public class ModuleTransformer {
	public static final String MODULE_ID = "moduleID";
	public static final String MODULE_NAME = "moduleName";
	public static final String MODULE_SLOT = "moduleSlot";
	public static final String MODULE_DEPENDENCY = "dependency";
	public static final String MODULE_ARCHIVE = "archive";
	public static final String PLATFORM_NAME = "platformName";
	public static final String PLATFORM_VERSION = "platformVersion";
	
	private ModuleTransformer() {
		// seal
	}
	
	public static ModuleVO fromDocument(Document doc) {
		ModuleVO module = new ModuleVO();
		module.setName(doc.get(MODULE_NAME));
		module.setSlot(doc.get(MODULE_SLOT));
		
		PlatformVO platform = new PlatformVO();
		platform.setName(doc.get(PLATFORM_NAME));
		platform.setVersion(doc.get(PLATFORM_VERSION));
		
		module.setPlatform(platform);
		
		for(IndexableField field : doc.getFields(MODULE_DEPENDENCY)) {
			String[] vals = StringUtils.split(field.stringValue(), "##");
			ModuleVO dep = new ModuleVO();
			dep.setName(vals[0]);
			dep.setSlot(vals[1]);
			
			module.getDependencies().add(dep);
		}
		
		return module;
	}

	public static Document toDocument(ModuleVO module) {
		Document document = new Document();
		document.add(new StringField(MODULE_ID, generateDependencyKey(module.getPlatform(), module), Field.Store.YES));
		document.add(new StringField(MODULE_NAME, module.getName(), Field.Store.YES));
		document.add(new StringField(MODULE_SLOT, module.getSlot(), Field.Store.YES));
		document.add(new StringField(PLATFORM_NAME, module.getPlatform().getName(), Field.Store.YES));
		document.add(new StringField(PLATFORM_VERSION, module.getPlatform().getVersion(), Field.Store.YES));
		
		for(ModuleVO mod : module.getDependencies()) {
			document.add(new StringField(MODULE_DEPENDENCY, generateDependencyKey(module.getPlatform(), mod), Field.Store.YES));
		}
		
		for(ArchiveVO archive : module.getArchives()) {
			document.add(new StringField(MODULE_ARCHIVE, archive.getSha1(), Field.Store.YES));
		}
		
		return document;
	}
	

	public static String generateDependencyKey(PlatformVO platform, ModuleVO module) {
		Validate.notNull(platform, "Platform is required.");
		Validate.notEmpty(platform.getName(), "Platform Name is required.");
		Validate.notEmpty(platform.getVersion(), "Platform Version is required.");

		Validate.notNull(module, "Module is required.");
		Validate.notEmpty(module.getName(), "Module Name is required.");
		Validate.notEmpty(module.getSlot(), "Module Slot is required.");
		
		StringBuilder index = new StringBuilder();
		index.append(module.getName()).append("##");
		index.append(module.getSlot()).append("##");
		index.append(platform.getName()).append("##").append(platform.getVersion());
		
		return index.toString();
	}


}
