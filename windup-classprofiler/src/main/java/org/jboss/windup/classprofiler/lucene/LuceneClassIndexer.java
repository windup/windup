package org.jboss.windup.classprofiler.lucene;

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
import org.jboss.windup.classprofiler.ClassIndexer;
import org.jboss.windup.classprofiler.exception.ClassIndexReaderException;
import org.jboss.windup.classprofiler.exception.ClassIndexWriteException;
import org.jboss.windup.classprofiler.lucene.transformer.ArchiveTransformer;
import org.jboss.windup.classprofiler.lucene.transformer.ClassTransformer;
import org.jboss.windup.classprofiler.metadata.ArchiveVO;
import org.jboss.windup.classprofiler.metadata.ClassVO;

public class LuceneClassIndexer implements ClassIndexer {

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
		
		Document document = ClassTransformer.toDocument(clz);
		
		if(StringUtils.isNotBlank(archive.getSha1())) {
			document.add(new StringField(ArchiveTransformer.ARCHIVE_SHA1, archive.getSha1(), Field.Store.YES));
		}
		
		if(StringUtils.isNotBlank(archive.getMd5())) {
			document.add(new StringField(ArchiveTransformer.ARCHIVE_MD5, archive.getMd5(), Field.Store.YES));
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
			query.add(new TermQuery(new Term(ClassTransformer.QUALIFIED_NAME, clz)), BooleanClause.Occur.MUST);
			
			int numResults = 100;
			ScoreDoc[] hits = searcher.search(query, numResults).scoreDocs;
			
			for(int i=0; i< hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				String[] values = doc.getValues(ClassTransformer.DEPENDENCY);
				
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

	public Collection<ClassVO> findClassesLeveragingDependency(String clz) throws ClassIndexReaderException {
		Set<ClassVO> clzs = new HashSet<ClassVO>();
		
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(indexDir);
			IndexSearcher searcher = new IndexSearcher(reader);

			BooleanQuery query = new BooleanQuery();
			query.add(new TermQuery(new Term(ClassTransformer.DEPENDENCY, clz)), BooleanClause.Occur.MUST);
			
			int numResults = 100;
			ScoreDoc[] hits = searcher.search(query, numResults).scoreDocs;
			
			for(int i=0; i< hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				ClassVO clzVO = ClassTransformer.fromDocument(doc);
				clzs.add(clzVO);
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
