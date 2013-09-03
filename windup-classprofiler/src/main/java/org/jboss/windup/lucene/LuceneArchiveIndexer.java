package org.jboss.windup.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
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
import org.jboss.windup.ArchiveIndexer;
import org.jboss.windup.exception.ArchiveIndexReaderException;
import org.jboss.windup.exception.ArchiveIndexWriteException;
import org.jboss.windup.metadata.ArchiveVO;

public class LuceneArchiveIndexer implements ArchiveIndexer {

	public static final String ARCHIVE_NAME = "archiveName";
	public static final String ARCHIVE_VERSION = "archiveVersion";
	public static final String ARCHIVE_SHA1 = "archiveSHA1";
	public static final String ARCHIVE_MD5 = "archiveMD5";
	
	private final Directory classIndexDir;
	private final Directory archiveIndexDir;
	private final IndexWriterConfig iwc;
	
	public LuceneArchiveIndexer(File luceneDirectory) throws IOException {
		File luceneArchiveIndexDirectory = new File(luceneDirectory, "archiveIndex");
		File luceneClassIndexDirectory = new File(luceneDirectory, "classIndex");
		
		archiveIndexDir = FSDirectory.open(luceneArchiveIndexDirectory);
		classIndexDir = FSDirectory.open(luceneClassIndexDirectory);
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
		iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(archiveIndexDir, iwc);
			writer.commit();
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public void addArchive(ArchiveVO archive) throws ArchiveIndexWriteException {
		Document document = new Document();
		document.add(new StringField(ARCHIVE_NAME, archive.getName(), Field.Store.YES));
		document.add(new StringField(ARCHIVE_VERSION, archive.getName(), Field.Store.YES));
		document.add(new StringField(ARCHIVE_SHA1, archive.getSha1(), Field.Store.YES));
		document.add(new StringField(ARCHIVE_MD5, archive.getMd5(), Field.Store.YES));
		
		for(String key : archive.getProperties().keySet()) {
			document.add(new StringField(key, archive.getProperties().get(key), Field.Store.YES));
		}
		
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(archiveIndexDir, iwc);
			writer.updateDocument(new Term(ARCHIVE_SHA1), document);
			writer.commit();
		}
		catch(Exception e) {
			throw new ArchiveIndexWriteException("Exception writing class: "+archive.getName(), e);
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public Collection<String> findArchiveByField(String field, String value) throws ArchiveIndexReaderException {
		Set<String> results = new HashSet<String>();
		
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(archiveIndexDir);
			IndexSearcher searcher = new IndexSearcher(reader);

			BooleanQuery query = new BooleanQuery();
			query.add(new TermQuery(new Term(field, value)), BooleanClause.Occur.MUST);
			
			int numResults = 100;
			ScoreDoc[] hits = searcher.search(query, numResults).scoreDocs;
			
			for(int i=0; i< hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				results.add(doc.get(ARCHIVE_NAME));
			}
		}
		catch(Exception e) {
			throw new ArchiveIndexReaderException("Exception querying archive by field: "+field, e);
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		
		return results;
	}

	public Collection<String> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException {
		Set<String> archiveSHA1References = new HashSet<String>();
		
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(classIndexDir);
			IndexSearcher searcher = new IndexSearcher(reader);

			BooleanQuery query = new BooleanQuery();
			query.add(new TermQuery(new Term(LuceneClassIndexer.QUALIFIED_NAME, clz)), BooleanClause.Occur.MUST);
			
			int numResults = 100;
			ScoreDoc[] hits = searcher.search(query, numResults).scoreDocs;
			
			for(int i=0; i< hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				archiveSHA1References.add(doc.get(ARCHIVE_SHA1));
			}
		}
		catch(Exception e) {
			throw new ArchiveIndexReaderException("Exception querying class: "+clz, e);
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		
		Set<String> archives = new HashSet<String>();
		//now, for each, find archive by SHA1.
		for(String sha1Ref : archiveSHA1References) {
			archives.addAll(findArchiveByField(ARCHIVE_SHA1, sha1Ref));
		}
		
		return archives;
	}

	public Collection<String> findArchiveLeveragingDependency(String clz) throws ArchiveIndexReaderException {
		Set<String> archiveSHA1References = new HashSet<String>();
		
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(classIndexDir);
			IndexSearcher searcher = new IndexSearcher(reader);

			BooleanQuery query = new BooleanQuery();
			query.add(new TermQuery(new Term(LuceneClassIndexer.DEPENDENCY, clz)), BooleanClause.Occur.MUST);
			
			int numResults = 100;
			ScoreDoc[] hits = searcher.search(query, numResults).scoreDocs;
			
			for(int i=0; i< hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				archiveSHA1References.add(doc.get(ARCHIVE_SHA1));
			}
		}
		catch(Exception e) {
			throw new ArchiveIndexReaderException("Exception querying class: "+clz, e);
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		
		Set<String> archives = new HashSet<String>();
		//now, for each, find archive by SHA1.
		for(String sha1Ref : archiveSHA1References) {
			archives.addAll(findArchiveByField(ARCHIVE_SHA1, sha1Ref));
		}
		
		return archives;
	}


}
