package org.jboss.windup.classprofiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.lucene.LuceneArchiveIndexer;
import org.jboss.windup.lucene.LuceneClassIndexer;
import org.jboss.windup.metadata.ArchiveVO;
import org.jboss.windup.metadata.ClassVO;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LuceneTest {

	private static LuceneArchiveIndexer lai;
	private static LuceneClassIndexer lci;
	
	@BeforeClass
	public static void testSetup() throws Exception {
		System.out.println("Setting up...");
		lai = new LuceneArchiveIndexer(new File("/tmp/lucene/test"));
		lci = new LuceneClassIndexer(new File("/tmp/lucene/test"));
		
		ArchiveVO avo = new ArchiveVO();
		avo.setName("lucene-test");
		avo.setVersion("1.0.0");
		avo.setSha1("abcdef");
		avo.setMd5("qrstuv");
		
		lai.addArchive(avo);
		
		for(int i=0; i<10000; i++) {
			ClassVO cvo = stubClass("com.bradsdavis.lucene.LuceneTest"+i, "java.io.File", "java.util.ArrayList", "java.util.List", "org.junit.Test");
			lci.addClass(avo, cvo);
		}
		System.out.println("Setup.");
	}
	
	@Test
	public void testArchiveBySha1() throws Exception {
		Collection<String> results = lai.findArchiveByField(LuceneArchiveIndexer.ARCHIVE_SHA1, "abcdef");
		Assert.assertTrue(results.size() == 1);
	}
	
	@Test
	public void testArchiveByName() throws Exception {
		Collection<String> results = lai.findArchiveByField(LuceneArchiveIndexer.ARCHIVE_NAME, "lucene-test");
		Assert.assertTrue(results.size() == 1);
		
		for(String result : results) {
			Assert.assertTrue(StringUtils.equals(result, "lucene-test"));
		}
		
	}
	
	@Test
	public void testArchiveByDependency() throws Exception {
		Collection<String> results = lai.findArchiveLeveragingDependency("java.io.File");
		Assert.assertTrue(results.size() == 1);

		for(String result : results) {
			Assert.assertTrue(StringUtils.equals(result, "lucene-test"));
		}
	}
	
	@Test
	public void testArchiveByClass() throws Exception {
		Collection<String> results = lai.findArchiveByQualifiedClassName("com.bradsdavis.lucene.LuceneTest0");
		Assert.assertTrue(results.size() == 1);

		for(String result : results) {
			Assert.assertTrue(StringUtils.equals(result, "lucene-test"));
		}
	}
	
	
	@Test
	public void testName() throws Exception {
		for(int i=0; i<10000; i++) {
			Collection<String> clzDependencies = lci.findClassDependenciesByQualifiedName("com.bradsdavis.lucene.LuceneTest"+i);
			Assert.assertTrue(clzDependencies.size() > 0);
		}
		Collection<String> dependencies = lci.findClassesLeveragingDependency("java.io.File");
		Assert.assertTrue(dependencies.size() > 0);
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
