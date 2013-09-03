package org.jboss.windup.classprofiler.lucene;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.classprofiler.lucene.LuceneArchiveIndexer;
import org.jboss.windup.classprofiler.lucene.LuceneClassIndexer;
import org.jboss.windup.classprofiler.lucene.transformer.ArchiveTransformer;
import org.jboss.windup.classprofiler.metadata.ArchiveVO;
import org.jboss.windup.classprofiler.metadata.ClassVO;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClassArchiveTest {

	private static final int totalNum = 100;
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
		

		ArchiveVO avo1 = new ArchiveVO();
		avo1.setName("lucene-second");
		avo1.setVersion("1.0.1");
		avo1.setSha1("ghijk");
		avo1.setMd5("vwxyz");
		
		lai.addArchive(avo);
		lai.addArchive(avo1);
		
		for(int i=0; i<totalNum; i++) {
			ClassVO cvo = stubClass("com.bradsdavis.lucene.LuceneTest"+i, "java.io.File", "java.util.ArrayList", "java.util.List", "org.junit.Test");
			lci.addClass(avo, cvo);
			System.out.println(i);
		}
		
		for(int i=0; i<totalNum; i++) {
			ClassVO cvo = stubClass("com.bradsdavis.lucene.LuceneTest"+i, "java.io.File", "java.util.ArrayList", "java.util.List", "org.junit.Test");
			lci.addClass(avo1, cvo);
			System.out.println(i);
		}
		
		System.out.println("Setup.");
	}
	
	@Test
	public void testArchiveBySha1() throws Exception {
		Collection<ArchiveVO> results = lai.findArchiveByField(ArchiveTransformer.ARCHIVE_SHA1, "abcdef");
		Assert.assertTrue(results.size() == 1);
	}
	
	@Test
	public void testArchiveByName() throws Exception {
		Collection<ArchiveVO> results = lai.findArchiveByField(ArchiveTransformer.ARCHIVE_NAME, "lucene-test");
		Assert.assertTrue(results.size() == 1);
		
		for(ArchiveVO result : results) {
			Assert.assertTrue(StringUtils.equals(result.getName(), "lucene-test"));
		}
		
	}
	
	@Test
	public void testArchiveByDependency() throws Exception {
		Collection<ArchiveVO> results = lai.findArchiveLeveragingDependency("java.io.File");
		Assert.assertTrue(results.size() == 2);

		Set<String> names = new HashSet<String>();
		for(ArchiveVO result : results) {
			names.add(result.getName());
		}
		
		Assert.assertTrue(names.contains("lucene-test"));
		Assert.assertTrue(names.contains("lucene-second"));
	}
	
	@Test
	public void testArchiveByClass() throws Exception {
		Collection<ArchiveVO> results = lai.findArchiveByQualifiedClassName("com.bradsdavis.lucene.LuceneTest0");
		Assert.assertTrue(results.size() == 2);

		Set<String> names = new HashSet<String>();
		for(ArchiveVO result : results) {
			names.add(result.getName());
		}
		
		Assert.assertTrue(names.contains("lucene-test"));
		Assert.assertTrue(names.contains("lucene-second"));

	}
	
	
	@Test
	public void testName() throws Exception {
		for(int i=0; i<totalNum; i++) {
			Collection<String> clzDependencies = lci.findClassDependenciesByQualifiedName("com.bradsdavis.lucene.LuceneTest"+i);
			Assert.assertTrue(clzDependencies.size() > 0);
		}
		Collection<ClassVO> dependencies = lci.findClassesLeveragingDependency("java.io.File");
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
