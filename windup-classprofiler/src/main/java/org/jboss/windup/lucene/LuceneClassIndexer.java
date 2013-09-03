package org.jboss.windup.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jboss.windup.ClassIndexer;
import org.jboss.windup.exception.ClassIndexReaderException;
import org.jboss.windup.exception.ClassIndexWriteException;
import org.jboss.windup.metadata.ArchiveVO;
import org.jboss.windup.metadata.ClassVO;

public class LuceneClassIndexer implements ClassIndexer {
	public static final String QUALIFIED_NAME = "qualifiedClassName";
	public static final String DEPENDENCY = "classDependency";
	
	private final Directory indexDir;
	private final IndexWriterConfig iwc;
	
	public LuceneClassIndexer(File luceneDirectory) throws IOException {
		File luceneIndexDirectory = new File(luceneDirectory, "classIndex");
		
		indexDir = FSDirectory.open(luceneIndexDirectory);
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
		iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(indexDir, iwc);
			writer.commit();
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
		
	}
	
	public void addClass(ArchiveVO archive, ClassVO clz) throws ClassIndexWriteException {
		Term unique = new Term("UNIQUE", clz.getQualifiedName() +"#"+archive.getSha1());
		
		Document document = new Document();
		document.add(new StringField(QUALIFIED_NAME, clz.getQualifiedName(), Field.Store.YES));
		
		if(clz.getDependencies() != null) {
			for(String dep : clz.getDependencies()) {
				document.add(new StringField(DEPENDENCY, dep, Field.Store.YES));
			}
		}
		
		if(StringUtils.isNotBlank(archive.getSha1())) {
			document.add(new StringField(LuceneArchiveIndexer.ARCHIVE_SHA1, archive.getSha1(), Field.Store.YES));
		}
		
		if(StringUtils.isNotBlank(archive.getMd5())) {
			document.add(new StringField(LuceneArchiveIndexer.ARCHIVE_MD5, archive.getMd5(), Field.Store.YES));
		}
		
		IndexWriter writer = null;
		try { 
			writer = new IndexWriter(indexDir, iwc);
			writer.updateDocument(unique, document);
			writer.commit();
		}
		catch(Exception e) {
			throw new ClassIndexWriteException("Exception writing class: "+clz.getQualifiedName(), e);
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public Collection<String> findClassDependenciesByQualifiedName(String clz)  throws ClassIndexReaderException {
		Set<String> clzs = new HashSet<String>();
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(indexDir);
			IndexSearcher searcher = new IndexSearcher(reader);

			BooleanQuery query = new BooleanQuery();
			query.add(new TermQuery(new Term(QUALIFIED_NAME, clz)), BooleanClause.Occur.MUST);
			
			int numResults = 100;
			ScoreDoc[] hits = searcher.search(query, numResults).scoreDocs;
			
			for(int i=0; i< hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				String[] values = doc.getValues(DEPENDENCY);
				
				clzs.addAll(Arrays.asList(values));
			}
		}
		catch(Exception e) {
			throw new ClassIndexReaderException("Exception querying class: "+clz, e);
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		
		return clzs;
	}

	public Collection<String> findClassesLeveragingDependency(String clz) throws ClassIndexReaderException {
		Set<String> clzs = new HashSet<String>();
		
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(indexDir);
			IndexSearcher searcher = new IndexSearcher(reader);

			BooleanQuery query = new BooleanQuery();
			query.add(new TermQuery(new Term(DEPENDENCY, clz)), BooleanClause.Occur.MUST);
			
			int numResults = 100;
			ScoreDoc[] hits = searcher.search(query, numResults).scoreDocs;
			
			for(int i=0; i< hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				clzs.add(doc.get(QUALIFIED_NAME));
			}
		}
		catch(Exception e) {
			throw new ClassIndexReaderException("Exception querying class: "+clz, e);
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		return clzs;
	}
	
}
