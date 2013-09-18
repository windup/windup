package org.jboss.windup.classprofiler.lucene;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.windup.classprofiler.metadata.ArchiveVO;
import org.jboss.windup.classprofiler.metadata.ClassVO;
import org.jboss.windup.classprofiler.metadata.ModuleVO;
import org.jboss.windup.classprofiler.metadata.PlatformVO;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModuleArchiveTest {

	private static final int totalNum = 1000;
	private static LuceneArchiveIndexer lai;
	private static LuceneClassIndexer lci;
	private static LuceneModuleIndexer lmi;
	
	@BeforeClass
	public static void testSetup() throws Exception {
		System.out.println("Setting up...");
		lai = new LuceneArchiveIndexer(new File("/tmp/lucene/test"));
		lci = new LuceneClassIndexer(new File("/tmp/lucene/test"));
		lmi = new LuceneModuleIndexer(new File("/tmp/lucene/test"));
		
		ArchiveVO avo = new ArchiveVO();
		avo.setName("lucene-test");
		avo.setVersion("1.0.0");
		avo.setSha1("abcdef");
		avo.setMd5("qrstuv");

		ArchiveVO avo1 = new ArchiveVO();
		avo1.setName("lucene-second");
		avo1.setVersion("1.0.1");
		avo1.setSha1("ghijk");
		avo1.setMd5("vwxyz");
		
		lai.addArchive(avo);
		lai.addArchive(avo1);

		//platform 6.0.0
		ModuleVO moduleEAP600_12 = new ModuleVO();
		moduleEAP600_12.setName("example-module");
		moduleEAP600_12.setSlot("1.2");

		ModuleVO moduleEAP600_13 = new ModuleVO();
		moduleEAP600_13.setName("example-module");
		moduleEAP600_13.setSlot("1.3");
		
		PlatformVO platformEAP600 = new PlatformVO();
		platformEAP600.setName("jboss-eap");
		platformEAP600.setVersion("6.0.0");
		moduleEAP600_12.setPlatform(platformEAP600);
		moduleEAP600_13.setPlatform(platformEAP600);
		
		moduleEAP600_12.getArchives().add(avo1);
		moduleEAP600_12.getArchives().add(avo);
		lmi.addModule(moduleEAP600_12);
		
		moduleEAP600_13.getArchives().add(avo1);
		lmi.addModule(moduleEAP600_13);
		

		//platform 6.1.0
		ModuleVO moduleEAP610_12 = new ModuleVO();
		moduleEAP610_12.setName("example-module");
		moduleEAP610_12.setSlot("1.2");

		PlatformVO platformEAP610 = new PlatformVO();
		platformEAP610.setName("jboss-eap");
		platformEAP610.setVersion("6.1.0");
		moduleEAP610_12.setPlatform(platformEAP610);
		
		moduleEAP610_12.getArchives().add(avo1);
		lmi.addModule(moduleEAP610_12);
		
		for(int i=0; i<totalNum; i++) {
			ClassVO cvo = stubClass("com.bradsdavis.lucene.LuceneTest"+i, "java.io.File", "java.util.ArrayList", "java.util.List", "org.junit.Test");
			lci.addClass(avo, cvo);
		}
		
		for(int i=0; i<totalNum; i++) {
			ClassVO cvo = stubClass("com.bradsdavis.lucene.LuceneTest"+i, "java.io.File", "java.util.ArrayList", "java.util.List", "org.junit.Test");
			lci.addClass(avo1, cvo);
		}
		
		System.out.println("Setup.");
	}
	
	@Test
	public void testModuleLoadingByClass() throws Exception {
		Collection<ModuleVO> modules = lmi.findModuleProvidingClass("com.bradsdavis.lucene.LuceneTest0");
		Assert.assertTrue(modules.size() == 3);
		
		Set<String> names = new HashSet<String>();
		for(ModuleVO module : modules) {
			names.add(module.getName()+":"+module.getSlot()+":"+module.getPlatform().getName()+":"+module.getPlatform().getVersion());
		}
		
		Assert.assertTrue(names.contains("example-module:1.2:jboss-eap:6.0.0"));
		Assert.assertTrue(names.contains("example-module:1.3:jboss-eap:6.0.0"));
		Assert.assertTrue(names.contains("example-module:1.2:jboss-eap:6.1.0"));

		System.out.println("Total Modules: "+modules.size());
	}
	
	public static ClassVO stubClass(String className, String ... deps) {
		ClassVO cvo = new ClassVO();
		cvo.setQualifiedName(className);

		List<String> dependencies = new ArrayList<String>();
		if(deps != null) {
			for(String dep : deps) {
				dependencies.add(dep);
			}
		}
		
		cvo.setDependencies(dependencies);
		return cvo;
	}
	
}
